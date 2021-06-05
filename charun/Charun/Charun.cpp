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

const char* VERSION="1.0.1";

int main(int argc, char** argv)
{
	char* pSrc = NULL;
	if (argc >= 2)
		pSrc = *(argv+1);

	char* pDst = NULL;
	if (argc >= 3)
		pDst =  *(argv+2);

	bool bVerbose = false;
	if (argc >= 4)
		bVerbose = true;

	if (!pSrc || !pDst)
		return 1;

	if (bVerbose)
		printf("Charun %s\n", VERSION);

	char buf[BUFSIZ];
    size_t size;

    FILE* source = fopen(pSrc, "rb");
	if (!source) {
		if (bVerbose)
			printf("first argument is invalid or not found\n");
		return 1;
	}

    FILE* dest = fopen(pDst, "wb");	    
	if (!dest) {
		if (bVerbose)
			printf("second argument is invalid or not found\n");
		fclose(source);
		return 1;
	}
	if (bVerbose)
		printf("replacing %s with %s\n", pDst, pSrc);

    while (size = fread(buf, 1, BUFSIZ, source)) {
        fwrite(buf, 1, size, dest);
    }
    fclose(source);
    fclose(dest);

	if (std::ferror(source) || std::ferror(dest)) {		
		if (bVerbose)
			printf("error\n");
		return 1;
	}
	if (bVerbose)
		printf("done\n");
	return 0;
}

