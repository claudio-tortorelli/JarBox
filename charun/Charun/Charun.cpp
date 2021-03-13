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

/**
 * this is Charun, a SelfGeneratingJar tool
 */
int main(int argc, char** argv)
{
	// some checks on arguments...
	if (argc != 2) {
		printf("invalid argument numbers\n");
		return 1;
	}
   
	// swap two jars (father is replaced by child...that's life!
	char buf[BUFSIZ];
    size_t size;

    FILE* source = fopen(argv[0], "rb");
	if (!source) {
		printf("first argument not valid\n");
		return 1;
	}

    FILE* dest = fopen(argv[1], "wb");	    
	if (!dest) {
		printf("second argument not valid\n");
		fclose(source);
		return 1;
	}
    while (size = fread(buf, 1, BUFSIZ, source)) {
        fwrite(buf, 1, size, dest);
    }

    fclose(source);
    fclose(dest);

	if (std::ferror(source) || std::ferror(dest)) {
		return 1;
	}
	return 0;
}

