package helpers.gens

object all extends AllGen

trait AllGen extends StringGen with NumberGen with DomainGen with RequestGen
