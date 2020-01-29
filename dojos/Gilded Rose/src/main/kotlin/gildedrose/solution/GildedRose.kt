package gildedrose.solution

open class Item(
	val name: String,
	var sellIn: Int,
	quality: Int
) {
	var quality: Int = quality
		set(value) {
			field = when {
				value > 50 -> 50
				value < 0  -> 0
				else       -> value
			}
		}

	fun sellByDateHasPassed() = 0 > sellIn

	override fun toString() = "Item(name=$name, sellIn=$sellIn, quality=$quality)"
}

class BackStagePass(sellIn: Int, quality: Int): Item("Backstage Pass", sellIn, quality)

class AgedBrie(sellIn: Int, quality: Int): Item("Aged Brie", sellIn, quality)

class Sulfuras(sellIn: Int, quality: Int): Item("Sulfuras", sellIn, quality)

class ConjuredItem(name: String, sellIn: Int, quality: Int): Item(name, sellIn, quality) {
	override fun toString() = "Conjured Item(name=$name, sellIn=$sellIn, quality=$quality)"
}

class GildedRoseShop(
	private val items: List<Item>
) {

	constructor(vararg items: Item): this(items.toList())

	fun updateItems(days: Int = 1) {
		repeat(days) {
			for (item in items) {
				if (item !is Sulfuras) {
					item.sellIn--
				}
				item.quality = item.quality + determineQualityChange(item)
			}
		}
	}

	private fun determineQualityChange(item: Item): Int {
		var change = when (item) {
			is BackStagePass -> {
				when {
					item.sellIn <= 5           -> 3
					item.sellIn <= 10          -> 2
					item.sellByDateHasPassed() -> -999
					else                       -> 1
				}
			}
			is Sulfuras      -> 0
			is ConjuredItem  -> -2
			is AgedBrie      -> 1
			else             -> -1
		}

		if (item.sellByDateHasPassed() && item !is BackStagePass) {
			change *= 2
		}

		return change
	}

}
