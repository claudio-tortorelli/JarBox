/**
 * Charun, part of SelfGeneratingJar
 * @author claudio.tortorelli
 * March 2021
 */
#if WIN32
#pragma warning(disable:4996) // needed to support fopen
#endif

#include <stdio.h>
#include <fstream>   

const char* VERSION="1.0.0";

/**
 * this is Charun, a SelfGeneratingJar tool
 */
int main(int argc, char** argv)
{
	printf("Charun ");
	printf(VERSION);
	printf("\n");

	// some checks on arguments...
	if (argc != 3) {
		printf("invalid argument numbers\n");
		return 1;
	}

	// swap two jars (father is replaced by child...that's life!
	char buf[BUFSIZ];
    size_t size;

    FILE* source = fopen(argv[1], "rb");
	if (!source) {
		printf("first argument is invalid or not found\n");
		return 1;
	}

    FILE* dest = fopen(argv[2], "wb");	    
	if (!dest) {
		printf("second argument is invalid or not found\n");
		fclose(source);
		return 1;
	}
	printf("replacing ");
	printf(argv[2]);
	printf("\n");

	printf("with ");
	printf(argv[1]);
	printf("\n");

    while (size = fread(buf, 1, BUFSIZ, source)) {
        fwrite(buf, 1, size, dest);
    }
    fclose(source);
    fclose(dest);

	if (std::ferror(source) || std::ferror(dest)) {		
		printf("error\n");
		return 1;
	}
	printf("done\n");
	return 0;
}

