cmake_minimum_required(VERSION 3.4.1)

file(GLOB rtmp_src *.c)

# NO_CRYPTO   -D宏
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

add_library(
        rtmp
        STATIC # librtmp.a
        ${rtmp_src})
