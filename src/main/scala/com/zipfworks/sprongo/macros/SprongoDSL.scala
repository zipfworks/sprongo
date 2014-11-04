package com.zipfworks.sprongo.macros

trait SprongoDSL
  extends FindDSL
  with UpdateDSL
  with RemoveDSL
  with InsertDSL
  with CommandDSL

object SprongoDSL extends SprongoDSL