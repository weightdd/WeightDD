# Artifact for "WDD: Weighted Delta Debugging"

We appreciate the constructive feedback, which our revision will incorporate,
as well as the clarifications in this response. 

**Apology and explanation for missing artifacts**: We apologize for the oversight regarding the "Open Science Policy" section prior to our submission. This led to a delay in releasing the artifacts associated with our paper. We have now made these artifacts publicly available at: https://github.com/weightdd/WeightDD.

## Review #1304A:

### Q1: Why is there nothing in the artifact repository?

### Q2: Can you comment on extensions to more creative "removal probability" factors a bit?

Yes, we can maintain the removal probability more creatively with the weight of the elements. A possible method is to introduce the weight as a new factor of the probability model of ProbDD, and the probability of elements with different weights shuld be initialized and updated differently. For example, a larger element should have a smaller initial probability than a smaller element.

## Review #1304B:

### Minor issues:

1. Good point, ddmin is designed with generality to perform a binary-search style minimization. When ddmin is used in different scenarios, the actual sizes of partitions can be different due to the different sizes of elements (e.g. tree nodes). Technically, this is not the problem of ddmin, but in practice, ddmin is seldom used directly and all the applications of ddmin (e.g. HDD, Perses) overlook this issue. That is why we propose WDD to address issue.

2. In Table 1, `T(s)` refers to the processing time in seconds (See the "Metrics" paragraph in Evaluation section).

3. In Table 1, `S(#)` refers to the size of the minimized results, counting by the number of tokens (See the "Metrics" paragraph in Evaluation section).

   We will add a more clear clarification in the title of Table 1.

### Q1: Why is the artifact not available? Please see the section on "Open Science Policy" in the call for papers.

### Q2: Please comment on threats to validity.



### Q3: Please comment on the limitations of your approach.

The main limitation of our approach is that it WDD mainly applies to tree-structured inputs and is effective when the sizes of elements (tree nodes) vary significantly. When the tree representation of the test input is not available or highly "balanced", WDD may have trivial improvement to ddmin. However, given the wide application of tree-based minimization techniques and the highly unbalanced characteristics of the trees of real-world inputs, WDD is still critical to improve the performance of test input minimization in real-world scenarios.

## Review #1304C:

### Q1: What makes token count a good representation of the likelihood of an element being related to a bug? In many real-world scenarios, other factors such as I/O operations, memory usage, or computational cost might be more relevant.

While factors such as I/O operations and memory usage can be relevant to the bug, they are unsuitable to represent the weight of elements for two reasons.

First, profiling the runtime information will bring great cost, thus hindering the performance of minimization.  Minimizing a large test case in real world scenarios usually involves executing the test case thousands of times. Capturing all these runtime information and using them to perform WDD will bring unacceptable computational and time consumption.

In addition, compared to token count which is static, runtime infomation like memory usage is dynamic, which makes them hard to utilize as weight to perform delta debugging. For instance, even if we can profile the memory usage of different elements (tree nodes) and partiton the list of elements into two subsets with similar memory usage, when the two variant program are executed, their memory usage can vary significantly (not equals to half of the previous memory usage), since the behavior of the new program can be largely different than before. Such a fact contradicts the nature of the delta debugging algorithm, which is a binary search process. 

### Q2: Does WDD account for dependencies between elements in the test input, e.g., one element relies on another for proper functioning? If not, how does the algorithm handle cases where removing one element might indirectly affect the behavior or relevance of other elements?

Indeed, dependencies between elements are prevalent. However, in practice, the minimization algorithms (including HDD and Perses) are normally performed in fix-point mode. That is to say, if some elements are deleted in the current deletion pass, the minimization will repeat the pass until no element can be further removed. This is how we did in our evaluation, and since it is a common issue for the test case minimization problem, the contibution of WDD should not be affected.

### Q3: What are the limitations of the static weight assignment in WDD? How would this static weight assignment compare with a dynamic weight adjustment strategy based on runtime characteristics or test outcomes in terms of the efficiency or accuracy?

As explained in Q1, while the static weight assignment by token count is not 100% accurate, it achieves higher efficiency and feasibility. The problem of using dynamic runtime information to assign weight is that the runtime of the program may vary significantly even if only a small part of the program changed. Therefore, if we want to leverage the runtime characteristics of the program to guide WDD, we need (1) leverage dynamic analysis tools to profile the program execution, (2) design dynamic partitioning strategy that adapts to the dynamic runtime information.  However, gievn our implementation of WDD in this paper mainly focus on language-agnostic test input minimization techniques, the two requirements above can hardly be realized. 

However, the "dynamic-weighted delta debugging (DWDD)" should be effective in language-specific minimization techniques, such as C-Reduce and ddSMT. Because these approaches are specifically designed for one specific language, runtime information can be obtained easier. Besides, the dynamic weight assignment can be implemented with the awareness of semantic information of the target language. Therefore, implementing DWDD in language-specific test input minimization techniques can be a promising topic for future work.

