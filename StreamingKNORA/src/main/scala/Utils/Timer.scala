package main.scala.Utils

import org.apache.log4j.{Level, LogManager, PropertyConfigurator}

object Timers{
  def time[R](block: => R): R = {  
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0)/1e9)
    result
  }
}

class Timers(var message: String){
  var logger = LogManager.getLogger("myLogger")
  var num = 0
  var elapsedTime:Long = 0
  var ifSecond = false
  def time[R](block: => R): R = {  
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    val e_time = t1 - t0
    println( message + " Elapsed time: " + e_time/1e9)
    logger.info(message + " Elapsed time: " + e_time/1e9)
    
    if(ifSecond){
      num+=1
      elapsedTime+=e_time
    }
    else{
      ifSecond = true
    }
    
    result
  }
  def printAverage()={
    logger.info( message + " Average Elapsed time: " + (elapsedTime.toDouble/num.toDouble)/1e9)
    println( message + " Average Elapsed time: " + (elapsedTime.toDouble/num.toDouble)/1e9)
  }
}