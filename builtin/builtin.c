#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define BUFFER_LENGTH 1024

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
    char * str = (char *) malloc(sizeof(char) * BUFFER_LENGTH);
    scanf("%s", str);
    return str;
}

int getInt() {
    int n;
    scanf("%d", &n);
    return n;
}

char * toString(int i) {
    char * str = (char *) malloc(20);
    sprintf(str, "%d", i);
    return str;
}

char * __mx_concatenateString(char * s1, char * s2) {
    int length = strlen(s1) + strlen(s2) + 1;
    char * str = (char *) malloc(length);
    str[0] = '\0';
    strcat(str, s1);
    strcat(str, s2);
    str[length - 1] = '\0';
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

int __mx_stringLength(char * str) {
    return strlen(str);
}

char * __mx_stringSubstring(char * str, int left, int right) {
    int length = right - left + 1;
    char * substr = (char *) malloc(length);
    strncat(substr, str + left, length);
    substr[length - 1] = '\0';
    return substr;
}

int __mx_stringParseInt(char * str) {
    return atoi(str);
}

int __mx_stringOrd(char * str, int pos) {
    return (int) str[pos];
}

char * __mx_malloc(int n) {
    return (char *) malloc(n);
}

