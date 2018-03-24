//#include <stdio.h>
//#include <stdlib.h>
//
//#include "domaci1.h"
//
//#define TRUE 1
//#define FALSE 0
//
//struct node {
//	int locked;
//	struct node* next = NULL;
//};
//
//struct node* tail = NULL;
//
//int lock_n_threads_with_timeout(int id, int* local, double timeout) {
//	// create node
//	struct node* myNode = (struct node*) malloc(sizeof(struct node));
//	myNode->locked = TRUE;
//
//	// save for later
//	local[0] = myNode;
//
//	// set myNode to last node and get previous
//	struct node* previousNode = lrk_get_and_set(tail, myNode);
//	if (previousNode != NULL) {
//		previousNode->next = myNode;
//
//		// spin until previous node sets my flag
//		while (myNode->locked) {
//			// TODO: backoff?
//		}
//
//	} else {
//		// we can go into critical section
//	}
//
//	return 0;
//}
//
//void unlock_n_threads_with_timeout(int id, int* local) {
//	struct node* myNode = local[0];
//
//	if (myNode->next != NULL) {
//		// unlock next node
//		myNode->next->locked = FALSE;
//
//		// free spent memory
//		free(local[0]);
//	} else {
//		// I'm last... we need to set tail to NULL
//		if (!lrk_compare_and_set(tail, myNode, NULL)) {
//			while (myNode->next == NULL) {}
//			myNode->next->locked = FALSE;
//			free(local[0]);
//		}
//	}
//
//}
//
//
//int main(void) {
//
//	start_timeout_mutex_n_threads_test(0.2);
//
//	return EXIT_SUCCESS;
//}
