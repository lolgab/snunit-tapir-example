package app
package html

import scalatags.Text.all.*

def renderIndex(tweets: Seq[Tweet]) = doctype("html")(
  html(
    head(
      title := "SNUnit Twitter",
      link(
        href := "/css/bootstrap.min.css",
        rel := "stylesheet"
      ),
      script(
        src := "/js/htmx.min.js"
      )
    ),
    body(
      nav(
        cls := "navbar navbar-dark bg-dark shadow-sm py-0",
        div(
          cls := "container",
          a(cls := "navbar-brand", "SNUnit Twitter"),
          span(cls := "navbar-text text-white"),
          "lolgab"
        )
      ),
      div(
        cls := "container",
        div(
          cls := "row justify-content-center",
          main(
            cls := "col-10",
            div(
              renderAddTweet(),
              renderTweets(tweets)
            )
          )
        )
      )
    )
  )
)

def renderTweets(tweets: Seq[Tweet]) = div(
  id := "tweets-list",
  tweets.map(tweet => renderTweet(tweet))
)

def renderTweet(tweet: Tweet) = div(
  div(
    cls := "card mb-2 shadow-sm",
    id := "tweet-58cf25e8-d40d-4ae8-8b5f-cf20fc9ab345",
    div(
      cls := "card-body",
      div(
        cls := "d-flex",
        img(
          cls := "me-4",
          src := s"https://ui-avatars.com/api/?background=random&rounded=true&name=${tweet.author}",
          attr("width") := "108"
        ),
        div(
          h5(
            cls := "card-title text-muted",
            tweet.author
          ),
          div(cls := "card-text lead mb-2", tweet.text)
        )
      )
    )
  )
)

def renderAddTweet() = form(
  hx.post := "/tweet",
  hx.target := "#tweets-list",
  hx.swap.afterbegin,
  div(
    label(`for` := "username", "Username:"),
    input(
      cls := "form-control",
      id := "username",
      name := "author",
      required := "true",
      placeholder := "please use your real identity 😗"
    )
  ),
  div(
    cls := "mb-3 row",
    label(`for` := "txtMessage", "Message:"),
    textarea(
      cls := "form-control",
      id := "txtMessage",
      rows := "3",
      name := "text",
      required := "true",
      placeholder := "What's up?"
    )
  ),
  div(
    cls := "d-grid gap-2 col-3 mx-auto mb-3",
    button(
      cls := "btn btn-primary text-center",
      tpe := "submit",
      "Tweet"
    )
  )
)

// helpers
private def nav = tag("nav")
private def main = tag("main")
