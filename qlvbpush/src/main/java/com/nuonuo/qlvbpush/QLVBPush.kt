package com.nuonuo.qlvbpush


fun QLVBPushHelper.init(action: QLVBPushHelper.() -> Unit): QLVBPushHelper {
    action.invoke(this)
    this.init()
    return this
}

class