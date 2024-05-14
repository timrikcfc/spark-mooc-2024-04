package monadparser

class MonadParser[T, Src](val p: Src =>(T, Src)) {

  def map[M](f: T => M): MonadParser[M, Src] =
    MonadParser {src =>
      val (word, rest) = p(src)
      (f(word), rest)
    }

  def flatMap[M](f: T => MonadParser[M, Src]): MonadParser[M, Src] =
    MonadParser{ src =>
      val (word, rest) = p(src)
      val mn = f(word)
      val res = mn.p(rest)
      res
    }

  def parse(src:Src): T = p(src)._1
}

object MonadParser {
  def apply[T, Src](f: Src => (T, Src)) = new MonadParser[T, Src](f)
}


object TextExecution {
  def main(args: Array[String]): Unit = {
    val str = "1997;Ford;F;true\n1901;Ford;T;false"
    case class Car(year:Int, mark:String, model: String,canDrive:Boolean)

    def StringField: MonadParser[String, String] = MonadParser[String, String] {
      str =>
        val idx = str.indexOf(";")
        if (idx > -1)
          (str.substring(0, idx), str.substring(idx+1))
        else
          (str, "")
    }

    def IntField: MonadParser[Int, String] = StringField.map(_.toInt)
    def BooleanField: MonadParser[Boolean, String] = StringField.map(_.toBoolean)

    val parser: MonadParser[Car, String] = for {
      year <- IntField
      mark <- StringField
      model <- StringField
      canDrive <- BooleanField
    } yield Car(year, mark, model, canDrive)

    val result: Array[Car] = str.split('\n').map(parser.parse)
    result

  }
}