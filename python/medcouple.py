import numpy as np

def kernel_same_partial(i, j, min_k, max_k, start_index):
    # partial matrix, shortcut for loop implementation
    # ensures we only add 0 for k times and ignore the other pairs as per paper
    if i == j: # this only happens k times, as the antidiagonal of the square of ties
        return 0
    else: # will be ignored
        return np.nan

def kernel_same_full(i, j, min_k, max_k, start_index):
    # full matrix, necessary for matrix implementation
    # simpler than paper version as it does not bother with values of k, mi, mj (troublesome as it's using a different i/j compared to the rest)
    # however this is theoretically equivalent since the result is just the median as we also end up with 0 for k times, and equal numbers of 1 and -1 to offset each other, just in different positions
    # also empirically equivalent based on tests
    return np.sign(j-i)

def kernel_same_monotonic(i, j, min_k, max_k, start_index):
    # full matrix preserving monotonicity, necessary for fast matrix implementation
    '''
    # uses values of k, mi, mj according to the paper formula just to make the matrix monotonic
    k = max_k - min_k + start_index
    mi = i - min_k + start_index
    mj = j - min_k + start_index
    res = mi + mj - start_index - k
    '''
    # simplify above
    res = i + j - min_k - max_k
    return np.sign(res)

# Not optimised, just testing my understanding first
# May be logically easier to understand than optimised versions, especially if you just assume either value for each argument as it's all the same
def medcouple_naive_loop(xs, kernel_same, med_centred=False, start_index=0):
    # here the formulas are the same regardless of whether indexing starts at 0 or 1 due to our kernel_same implementations
    med = np.median(xs)
    if (med_centred):
        def kernel_diff_num(xi, xj):
            return xj + xi
        def shift(x):
            return x - med
    else:
        def kernel_diff_num(xi, xj):
            return (xj - med) - (med - xi)
        def shift(x):
            return x
    def kernel_diff(xi, xj):
        num = kernel_diff_num(xi, xj)
        den = xj - xi
        return num / den
    xs = sorted(xs)
    xis = [(shift(x), i+start_index) for i, x in enumerate(xs) if x <= med]
    xjs = [(shift(x), j+start_index) for j, x in enumerate(xs) if x >= med]
    ks = [k+start_index for k, x in enumerate(xs) if x == med]
    if len(ks):
        min_k = min(ks)
        max_k = max(ks)
    else: # these values will not be used
        min_k = np.nan
        max_k = np.nan
    buffer = []
    for xj, j in reversed(xjs):
        for xi, i in reversed(xis):
            # if xi == med and xj == med:
            if xi == xj: # simplified as this is only possible when they equal the median. this actually gets rid of an ipykernel_launcher Runtime warning and speeds it up
                item = kernel_same(i, j, min_k, max_k, start_index)
            else:
                item = kernel_diff(xi, xj)
            buffer.append(item)
    # print("reshaped\n", np.reshape(buffer, (len(xjs), len(xis))))
    return np.nanmedian(buffer)

def gen_matrix_same_partial(i_idx, j_idx, z_index, zs):
    same_ref = j_idx == i_idx
    return np.where(same_ref, np.zeros_like(same_ref), np.full_like(same_ref, np.nan, dtype=np.float64))

def gen_matrix_same_full(i_idx, j_idx, z_index, zs):
    same_ref = j_idx - i_idx
    return np.where(same_ref == 0, np.zeros_like(same_ref), np.where(same_ref > 0, np.ones_like(same_ref), np.full_like(same_ref, -1)))

def gen_matrix_same_monotonic(i_idx, j_idx, z_index, zs):
    k_idx = z_index[zs == 0]
    max_k = np.max(k_idx)
    min_k = np.min(k_idx)
    same_ref = i_idx + j_idx - min_k - max_k
    return np.where(same_ref == 0, np.zeros_like(same_ref), np.where(same_ref > 0, np.ones_like(same_ref), np.full_like(same_ref, -1)))

# slightly slower than statsmodels but hopefully more intuitive as logic is close to the loop version
def medcouple_naive_matrix(xs, gen_matrix_same): # kernel_same=gen_matrix_same, med_centred=True, start_index=0
    xs = np.sort(xs) # sorts and turns it into numpy array
    # med = np.median(xs) # more efficient not to redo the sorting
    n = len(xs)
    if n % 2 == 0:
        med = np.mean([xs[n // 2 - 1], xs[n // 2]])
    else:
        med = xs[(n - 1) // 2]
    zs = np.flip(xs - med)
    zis = zs[zs <= 0]
    zjs = zs[zs >= 0]
    zjs = zjs[:, None] # transpose to vertical
    num = zjs + zis
    den = zjs - zis
    if len(zs[zs == 0]): # any ties with median
        z_index = np.flip(np.indices(np.shape(zs))[0])
        zis_idx = z_index[zs <= 0]
        zjs_idx = z_index[zs >= 0]
        zjs_idx = zjs_idx[:, None] # transpose to vertical
        i_idx = np.ones_like(zjs_idx) * zis_idx
        j_idx = np.ones_like(zis_idx) * zjs_idx
        same = gen_matrix_same(i_idx, j_idx, z_index, zs)
    else:
        same = np.full_like(den, np.nan)
    with np.errstate(invalid='ignore'): # since we are handling the divide by zero
        res = np.where(den == 0, same, num/den)
    # print(res)
    return np.nanmedian(res)
