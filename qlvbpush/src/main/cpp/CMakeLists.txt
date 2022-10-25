

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("qlvbpush")


add_library( # Sets the name of the library.
        qlvbpush


        SHARED


        qlvbpush.cpp)


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


target_link_libraries( # Specifies the target library.
        qlvbpush

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})