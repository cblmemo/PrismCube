#include <stdio.h>
#include <string.h>

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

char * __mx_concatenateString(char * s1, char * s2) {
    char str[MAX_RETURN_STRING_LENGTH];
    str[0] = '\0';
    strcat(str, s1);
    strcat(str, s2);
    return str;
}

unsigned char __mx_stringLt(char * s1, char * s2) {
    return strcmp(s1, s2) < 0;
}

unsigned char __mx_stringLe(char * s1, char * s2) {
    return strcmp(s1, s2) <= 0;
}

unsigned char __mx_stringGt(char * s1, char * s2) {
    return strcmp(s1, s2) > 0;
}

unsigned char __mx_stringGe(char * s1, char * s2) {
    return strcmp(s1, s2) >= 0;
}

unsigned char __mx_stringEq(char * s1, char * s2) {
    return strcmp(s1, s2) == 0;
}

unsigned char __mx_stringNe(char * s1, char * s2) {
    return strcmp(s1, s2) != 0;
}

