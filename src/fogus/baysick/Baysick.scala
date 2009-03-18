package fogus.baysick {
  import scala.collection.mutable.HashMap

  class Baysick {
    abstract sealed class BasicLine
    case class PrintString(num: Int, s: String) extends BasicLine
    case class PrintResult(num:Int, fn:Function0[String]) extends BasicLine
    case class PrintVariable(num: Int, s: Symbol) extends BasicLine
    case class PrintNumber(num: Int, number: BigInt) extends BasicLine
    case class Goto(num: Int, to: Int) extends BasicLine
    case class Input(num: Int, name: Symbol) extends BasicLine
    case class Let(num:Int, fn:Function0[Unit]) extends BasicLine
    case class End(num: Int) extends BasicLine

    val lines = new HashMap[Int, BasicLine]
    val binds = new HashMap[Symbol, Any]

    case class Assignr(sym:Symbol) {
      def :=(value:Any):Function0[Unit] = {
        return new Function0[Unit] {
          def apply() = {
            binds(sym) = value
          }
        }
      }
    }

    case class Appendr(str: String) {
      var appendage = str

      def this(key:Symbol) = this(binds(key).toString)

      def %(key:Symbol):Function0[String] = {
        return new Function0[String] {
          def apply():String = {
            appendage.concat(binds(key).toString)
          }
        }
      }
    }

    case class LineBuilder(num: Int) {
      def END() = lines(num) = End(num)

      object PRINT {
        def apply(str:String) = lines(num) = PrintString(num, str)
        def apply(number: BigInt) = lines(num) = PrintNumber(num, number)
        def apply(s: Symbol) = lines(num) = PrintVariable(num, s)
        def apply(fn:Function0[String]) = lines(num) = PrintResult(num, fn)
      }

      object INPUT {
        def apply(name: Symbol) = lines(num) = Input(num, name)
      }

      object LET {
        def apply(fn:Function0[Unit]) = lines(num) = Let(num, fn)
      }

      object GOTO {
        def apply(to: Int) = lines(num) = Goto(num, to)
      }
    }

    private def gotoLine(line: Int) {
      lines(line) match {
        case PrintNumber(_, number:BigInt) => {
          println(number)
          gotoLine(line + 10)
        }
        case PrintString(_, s:String) => {
          println(s)
          gotoLine(line + 10)
        }
        case PrintResult(_, fn:Function0[String]) => {
          println(fn())
          gotoLine(line + 10)
        }
        case PrintVariable(_, s:Symbol) => {
          val value = binds(s)
          println(value)
          gotoLine(line + 10)
        }
        case Input(_, name) => {
          val entry = readLine
          binds(name) = entry
          gotoLine(line + 10)
        }
        case Let(_, fn:Function0[Unit]) => {
          fn()
          gotoLine(line + 10)
        }
        case Goto(_, to) => gotoLine(to)
        case End(_) => {
          println("-- Done at line " + line)
        }
      }
    }

    def RUN {
      gotoLine(lines.keys.toList.sort((l,r) => l < r).first)
    }

    implicit def int2LineBuilder(i: Int) = LineBuilder(i)
    implicit def string2Appendr(str:String) = Appendr(str)
    implicit def symbol2Assignr(sym:Symbol) = Assignr(sym)
  }
}
