package com.zipfworks.sprongo.macros

import reactivemongo.core.commands.GetLastError

trait RemoveDSL {

  case class RemoveQuery[S](
    selector: S,
    writeConcern: GetLastError = GetLastError(),
    multi: Boolean = false
  ){
    def writeConcern(wr: GetLastError): RemoveQuery[S] = this.copy(writeConcern = wr)
    def multi(b: Boolean): RemoveQuery[S] = this.copy(multi = b)
  }

  def remove[S](selector: S): RemoveQuery[S] = RemoveQuery(selector = selector)

}

object RemoveDSL extends RemoveDSL
