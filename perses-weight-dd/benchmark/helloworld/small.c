#include<stdio.h>

double d = 0.10;
struct S {
    int f1;
    int f2;
};

void foo(struct S s, char str[]) {
    double v = s.f2 + s.f2 * d;
    printf("%s %f\n", str, v);
}

void f1() {
    printf("f1\n");
}

void f2() {
    printf("f2\n");
}

void f3() {
    printf("f3\n");
    printf("f3\n");
    printf("f3\n");
}

void f4() {
    printf("f4\n");
    printf("f4\n");
    printf("f4\n");
    printf("f4\n");
    printf("f4\n");
    printf("f4\n");
    printf("f4\n");
}

void f5() {
    printf("f5\n");
}

int main() {
    unsigned int a = 1;
    char b[] = "first";
    char c[] = "second";
    if(a) {
        struct S s1;
        s1.f1 = 1;
        s1.f2 = 4000;
        struct S s2;
        s2.f1 = 2;
        s2.f2 = 2000;
        foo(s1, b);
        foo(s2, c);
    }
    printf("helloworld\n");
    printf("helloworld\n");
    printf("helloworld\n");
    printf("helloworld\n");
    printf("helloworld\n");
    printf("helloworld\n");
    printf("helloworld\n");
    printf("helloworld\n");
    printf("helloworld\n");
    printf("helloworld\n");
    printf("helloworld\n");
    printf("bug!\n");
    return 0;
}