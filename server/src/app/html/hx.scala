package app.html

import scalatags.Text.all.*

object hx:
  val post = attr("hx-post")

  val target = attr("hx-target")

  object swap:
    private val swap = attr("hx-swap")
    val afterbegin = swap := "afterbegin"
    val beforeend = swap := "beforeend"
