package gildedrose.kata

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class GildedRoseTest {

	lateinit var cut: GildedRoseShop

	@Test
	fun `quality and sellin values are lowered each day`() {
		val stick = aSimpleStick()
		cut = GildedRoseShop(stick)
		cut.updateItems()

		stick.sellIn shouldBe 0
		stick.quality shouldBe 0
	}
}

fun aSimpleStick() = Item("A Simple Stick", 1, 1)

fun bootsOfWaterWalking() = Item("Boots Of Water Walking", 2, 5)
