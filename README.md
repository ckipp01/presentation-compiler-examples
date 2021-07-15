# Presentation compiler examples

These are code samples to go along with my [An intro to the Scala Presentation
Compiler](https://www.chris-kipp.io/blog/an-intro-to-the-scala-presentation-compiler)
blog post. They are small standalone examples of how to mimic the LSP
[`textDocument/selectionRange`](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#textDocument_selectionRange)
feature from
[Metals](https://scalameta.org/metals/blog/2021/07/14/tungsten#add-support-for-textdocumentselectionrange).

## Trying them out

You can try out the example by simply running each module via Mill.

For the Scala 3 example
```sh
./mill scala-3-presentation-compiler-examples.run
```

For the Scala 2 example
```sh
./mill scala-2-presentation-compiler-examples.run
```

These will print out the selection ranges for the code provided in the
`ourScalaCode` variable in each of the `SelectionRanges.scala` files.
