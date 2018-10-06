#include <vector>
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <fstream>
#include <map>
#include <cstdint>
#include <cmath>
#include <queue>
#include <string>
#include <unordered_map>
using namespace std;

int main(int argc,char *argv[]){
    ifstream myfile (argv[1]);
    unordered_map<string,int> m;
    if (myfile.is_open()){
        vector<string> tokens;
        while (getline (myfile,tokens,' ')){
            for(int i=0;i<tokens.size();i++){
                cout<<tokens[i]<<endl;
            }
            //cout<<line<<endl;
            /*int pos=0;
            while ((pos = str.find(delimiter, prev)) != std::string::npos)
            {
                strings.push_back(str.substr(prev, pos - prev));
                prev = pos + 1;
            }*/
        }
        myfile.close();
    }
    return 0;
}
