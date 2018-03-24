
#ifndef VEZBA3_H_
#define VEZBA3_H_

#define THREAD_COUNT 120

/*
 * Performs a test for the spinlock implementation.
 * There are ~30K locks and unlocks in total.
 *
 * The function expects the following to be implemented:
 * void lock_n_threads(int id, int* local)
 * void unlock_n_threads(int id, int* local)
 */
extern void start_mutex_n_threads_test();

/*
 * Performs a compare of variable var with value old_val
 * If they are same, var is set to new_val, and 1 is returned
 * If they are not same, var is unchanged and 0 is returned
 */
extern int lrk_compare_and_set(int* var, int old_val, int new_val);

/*
 * Sets the current thread into sleep state for the given number
 * of milliseconds.
 */
extern void lrk_sleep(long millis);

/*
 * Gets the value of variable var and performs
 * the specified operation afterwards, atomically
 */
extern int lrk_get_and_increment(int* var);
extern int lrk_get_and_decrement(int* var);
extern int lrk_get_and_add(int* var, int to_add);
extern int lrk_get_and_sub(int* var, int to_sub);
extern int lrk_get_and_add_with_mod(int* var, int to_add, int mod);
extern int lrk_get_and_set(int* var, int to_set);

/*
 * Retrieves the time ellapsed from the start of the process.
 * Time is retrieved in seconds.
 */
extern double lrk_get_time();

#endif /* VEZBA3_H_ */
