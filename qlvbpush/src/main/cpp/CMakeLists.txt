
cmake_minimum_required(VERSION 3.18.1)


#直接源码 干
add_subdirectory(librtmp)

#导入头文件
include_directories(librtmp libx264/include libfaac/include)

#设置264 静态库路径
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/libx264/libs/${CMAKE_ANDROID_ARCH_ABI}")
#设置faac 静态库
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/libfaac/libs/${CMAKE_ANDROID_ARCH_ABI}")
#就几个我也全部all in
file(GLOB src_files *.cpp)
add_library(
        qlvbpush
        SHARED
        ${src_files}
)


find_library(
        log-lib


        log)


target_link_libraries(
        qlvbpush
        rtmp
        x264
        faac

        ${log-lib})