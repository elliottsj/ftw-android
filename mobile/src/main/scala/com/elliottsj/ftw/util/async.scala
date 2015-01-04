package com.elliottsj.ftw.util

import android.os.AsyncTask

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.Try

/**
 * Integrate this trait to define an implicit execution context
 * using Android's AsyncTask thread pool executor.
 */
trait AsyncTaskContext {
  implicit val executor = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
}

/**
 * A task which should be executed after the specified duration.
 *
 * Example:
 * {{{
 *   import scala.concurrent.ExecutionContext.global
 *   import scala.concurrent.duration._
 *
 *   def doLater = DelayedTask(3.seconds) {
 *     println("Three seconds later")
 *   }
 * }}}
 */
object DelayedTask {
  def apply(delayBy: Duration)(body: => Unit)(implicit executor: ExecutionContext) = Future {
    // Wrap Await.ready in a Try so it returns after the expected java.util.concurrent.TimeoutException
    Try(Await.ready(Promise().future, delayBy))
    body
  }
}
