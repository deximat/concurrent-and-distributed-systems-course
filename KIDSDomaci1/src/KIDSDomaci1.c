#include <stdio.h>
#include <stdlib.h>

#include "domaci1.h"

#define TRUE 1
#define FALSE 0

struct node {
	int locked;
	struct node* next;
} node;

struct node* tail = (struct node *) NULL;

int balans = 0;

int lock_n_threads_with_timeout(int id, int* local, double timeout) {
	double startTime = lrk_get_time();
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
	struct node* previousNode = (struct node *) lrk_get_and_set((int *) &tail,
			(int) myNode);
	if (previousNode != NULL) {
		previousNode->next = myNode;

		// spin until previous node sets my flag
		int waitingTimes = 0;
		while (myNode->locked) {
			// TODO: backoff?
			lrk_sleep(1);
			waitingTimes++;
			if (waitingTimes > 100) {
				puts("locked waiting...");
			}

			if (lrk_get_time() - startTime > timeout) {
				int timeoutSuccess = lrk_compare_and_set(&(myNode->locked),
				TRUE, FALSE);
				if (timeoutSuccess) {
					return 0;
				} else {
					// we actually got critical section just now
				}
			}
		}

	} else {
		// we can go into critical section
		myNode->locked = FALSE;
	}

	return 1;

}

int unlockNode(struct node* node) {
	if (node->locked == TRUE) {
		return lrk_compare_and_set(&(node->locked), TRUE,
		FALSE);
	}
}

void unlock_n_threads_with_timeout(int id, int* local) {
	lrk_get_and_decrement(&balans);
//	puts("UNLOCK");
//	printf("unlock %d local: %d balans %d\n", id, local[0], balans);
//	lrk_sleep(1000);

	struct node* myNode = (struct node *) local[0];

	struct node* currentNode = myNode;

	while (1) {

		if (currentNode->next != NULL) {

			if (unlockNode(currentNode->next)) {
				// didn't time out, so I can kill this node
				free((void *) currentNode);
				break;
			} else {
				// time outed, see in next loop what can we do with that node
				void * nodeToFree = currentNode;
				currentNode = currentNode->next;
				free(nodeToFree);
			}

		} else {
			// I'm last... we need to set tail to NULL
			if (!lrk_compare_and_set((int*) &tail, (int) currentNode,
					(int) NULL)) {
				// didn't succeed to set tail to null, so new node is there
				// waiting for new node to become my next
				while (currentNode->next == NULL) {
					lrk_sleep(1);
					puts("last waiting...");
				}
				// now it will be done in next loop
			} else {
				// success to set tail to null, just break
				free(currentNode);
				break;
			}
		}
	}

}

int main(void) {
	puts("test timeout");
	start_timeout_mutex_n_threads_test(0.2);

	return EXIT_SUCCESS;
}
