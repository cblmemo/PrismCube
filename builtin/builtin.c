#include <stdio.h>

#define MAX_RETURN_STRING_LENGTH 1024

void print(char * str) {
    printf("%s", str);
}

void println(char * str) {
    printf("%s\n", str);
}

void printInt(int n) {
    printf("%d", n);
}

void printlnInt(int n) {
    printf("%d\n", n);
}

char * getString() {
    char str[MAX_RETURN_STRING_LENGTH];
    scanf("%s", str);
    return str;
}

int getInt() {
    int n;
    scanf("%d", &n);
    return n;
}

char * toString(int i) {
    char str[MAX_RETURN_STRING_LENGTH];
    sprintf(str, "%d", i);
    return str;
}