#include <stdio.h>
#include <stdlib.h>

#include "vezba3.h"

#define TRUE 1
#define FALSE 0

struct node {
	int locked;
	struct node* next;
} node;

struct node* tail = (struct node *) NULL;

int balans = 0;

void lock_n_threads(int id, int* local) {
	lrk_get_and_increment(&balans);
//	puts("LOCK");
//	printf("Locking id: %d local: %d balans: %d\n", id, local[0], balans);
//	lrk_sleep(2000);

	// create node
	struct node* myNode = (struct node*) malloc(sizeof(struct node));
	myNode->locked = TRUE;
	myNode->next = NULL;

	// save for later
	local[0] = (int) myNode;

	// set myNode to last node and get previous
	struct node* previousNode = (struct node *) lrk_get_and_set((int *) &tail, (int) myNode);
	if (previousNode != NULL) {
		previousNode->next = myNode;

		// spin until previous node sets my flag
		int waitingTimes = 0;
		while (myNode->locked) {
			// TODO: backoff?
			lrk_sleep(10);
			waitingTimes++;
			if (waitingTimes > 100) {
				puts("locked waiting...");
			}
		}

	} else {
		// we can go into critical section
		myNode->locked = FALSE;
	}

}

void unlock_n_threads(int id, int* local) {
	lrk_get_and_decrement(&balans);
//	puts("UNLOCK");
//	printf("unlock %d local: %d balans %d\n", id, local[0], balans);
//	lrk_sleep(1000);

	struct node* myNode = (struct node *) local[0];

	if (myNode->next != NULL) {
		// unlock next node
		myNode->next->locked = FALSE;

		// free spent memory
		free((void *)local[0]);
	} else {
		// I'm last... we need to set tail to NULL
		if (!lrk_compare_and_set((int*) &tail, (int) myNode, (int) NULL)) {
			while (myNode->next == NULL) {
				lrk_sleep(10);
				puts("last waiting...");
			}
			myNode->next->locked = FALSE;
			free((void *)local[0]);
		}
	}


}


int main(void) {
	puts("test");

	start_mutex_n_threads_test();

	return EXIT_SUCCESS;
}
