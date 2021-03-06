package docspell.analysis.nlp

import scala.jdk.CollectionConverters._

import cats.Applicative
import cats.implicits._

import docspell.common._

import edu.stanford.nlp.pipeline.{CoreDocument, StanfordCoreNLP}

object StanfordNerClassifier {

  /** Runs named entity recognition on the given `text`.
    *
    * This uses the classifier pipeline from stanford-nlp, see
    * https://nlp.stanford.edu/software/CRF-NER.html. Creating these
    * classifiers is quite expensive, it involves loading large model
    * files. The classifiers are thread-safe and so they are cached.
    * The `cacheKey` defines the "slot" where classifiers are stored
    * and retrieved. If for a given `cacheKey` the `settings` change,
    * a new classifier must be created. It will then replace the
    * previous one.
    */
  def nerAnnotate[F[_]: Applicative](
      cacheKey: String,
      cache: PipelineCache[F]
  )(settings: StanfordNerSettings, text: String): F[Vector[NerLabel]] =
    cache
      .obtain(cacheKey, settings)
      .map(crf => runClassifier(crf, text))

  def runClassifier(nerClassifier: StanfordCoreNLP, text: String): Vector[NerLabel] = {
    val doc = new CoreDocument(text)
    nerClassifier.annotate(doc)
    doc.tokens().asScala.collect(Function.unlift(LabelConverter.toNerLabel)).toVector
  }

}
