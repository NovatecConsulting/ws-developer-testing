package gildedrose.kata

class Item(
	val name: String,
	var sellIn: Int,
	var quality: Int
) {
	override fun toString() = "Item(name=$name, sellIn=$sellIn, quality=$quality)"
}

class GildedRoseShop(
	private val items: List<Item>
) {

	constructor(vararg items: Item): this(items.toList())

	fun updateItems(days: Int = 1) {
		repeat(days) {
			for (item in items) {
				println("Updating $item")
			}
		}
	}

}
