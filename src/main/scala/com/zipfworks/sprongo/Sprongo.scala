package com.zipfworks.sprongo

import reactivemongo.core.commands.LastError

object Sprongo {

  def grabErrors(errors: List[LastError]): List[String] = errors.map(le => le.ok match {
    case true => None
    case false => Some(le.getMessage())
  }).flatten.toList

}
