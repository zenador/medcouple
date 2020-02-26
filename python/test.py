from operator import itemgetter
from timeit import timeit, default_timer as timer
from functools import partial

import numpy as np
from statsmodels.stats.stattools import medcouple as stms_medcouple

from medcouple import medcouple_naive_loop, kernel_same_monotonic, kernel_same_full, kernel_same_partial, medcouple_naive_matrix, gen_matrix_same_monotonic, gen_matrix_same_full, gen_matrix_same_partial

test_arrs = [
    [0,1,2,2,3],
    [1,2,2,2,3,4],
    [1,2,2,2,2,3,4],
    [0.2, 0.17, 0.08, 0.16, 0.88, 0.86, 0.09, 0.54, 0.27, 0.14],
    [1] * 10 + [5] * 3,
    [10.0]*480 + [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20],
    np.random.poisson(1, size=500),
    np.random.rand(500),
]

def test():
    # If no output besides average timings per function, means all passed
    algos = []
    algo_names = []
    statsmodels_medcouple = lambda x: stms_medcouple(x).flat[0]
    algos.append(statsmodels_medcouple)
    algo_names.append("statsmodels_medcouple")
    for gen_matrix_same in [gen_matrix_same_monotonic, gen_matrix_same_full, gen_matrix_same_partial]:
        algos.append(lambda x: medcouple_naive_matrix(x, gen_matrix_same))
        algo_names.append("naive_matrix_{}".format(gen_matrix_same))
    for kernel_same in [kernel_same_monotonic, kernel_same_full, kernel_same_partial]:
        for med_centred in [False, True]:
            for start_index in [0, 1]:
                algos.append(lambda x: medcouple_naive_loop(x, kernel_same, med_centred, start_index))
                algo_names.append("naive_loop_{}_{}_{}".format(kernel_same, med_centred, start_index))
    overall_timings = []
    for arr in test_arrs:
        results = []
        timings = []
        for f in algos:
            # start = timer()
            results.append(f(arr))
            # end = timer()
            # timings.append(end - start)
            timings.append(timeit(partial(f, arr), number=10))
        overall_timings.append(timings)
        if not all(i == results[0] for i in results): # np.equal.reduce(results) does not work in odd cases
            print(arr, results)
    timings_list = sorted(zip(algo_names, np.mean(overall_timings, axis=0)), key=itemgetter(1), reverse=True)
    print(timings_list)

test_arr = [0,1,2,2,3]
# print(stms_medcouple(test_arr).flat[0])
# print(medcouple_naive_loop(test_arr, kernel_same_partial, True, 0))
print(medcouple_naive_matrix(test_arr, gen_matrix_same_partial))
test()
