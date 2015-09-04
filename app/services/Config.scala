package services


object BlottrConfig extends AwsInstanceTags {

  lazy val conf = readTag("Stage") match {
    case Some("PROD") =>    new ProdBlottrConfig
    case Some("CODE") =>    new CodeBlottrConfig
    case _ =>               new DevBlottrConfig
  }

  def apply() = {
    conf
  }
}

sealed trait BlottrConfig {
  def pandaDomain: String
  def composerUrl: String

}

class DevBlottrConfig extends BlottrConfig {
  override val pandaDomain = "local.dev-gutools.co.uk"
  override val composerUrl = "https://composer.local.dev-gutools.co.uk"
}

class CodeBlottrConfig extends BlottrConfig {
  override val pandaDomain = "code.dev-gutools.co.uk"
  override val composerUrl = "https://composer.code.dev-gutools.co.uk"
}

class ProdBlottrConfig extends BlottrConfig {
  override val pandaDomain = "gutools.co.uk"
  override val composerUrl = "https://composer.gutools.co.uk"
}
