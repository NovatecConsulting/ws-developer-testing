package gildedrose.solution

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

	@Test
	fun `quality degrades twice as fast when sell-by date is passed`() {
		val bootsOfWaterWalking = bootsOfWaterWalking()
		cut = GildedRoseShop(bootsOfWaterWalking)
		cut.updateItems(days = 3)

		bootsOfWaterWalking.sellIn shouldBe -1
		bootsOfWaterWalking.quality shouldBe 1
	}

	@Test
	fun `quality is never negative`() {
		val stick = aSimpleStick()
		cut = GildedRoseShop(stick)
		cut.updateItems(days = 10)

		stick.quality shouldBe 0
	}

	@Test
	fun `aged brie gains quality`() {
		val cheese = agedBrie()
		cut = GildedRoseShop(cheese)

		cut.updateItems(days = 10)

		cheese.quality shouldBe 22
	}

	@Test
	fun `quality is never above 50`() {
		val cheese = agedBrie()
		cut = GildedRoseShop(cheese)

		cut.updateItems(days = 100)

		cheese.quality shouldBe 50
	}

	@Test
	fun `legendary items never depreciate`() {
		val legendaryHammer = laserPoweredGoblinSmasher()
		cut = GildedRoseShop(legendaryHammer)

		cut.updateItems(days = 100)

		legendaryHammer.sellIn shouldBe 10
		legendaryHammer.quality shouldBe 10
	}

	@Test
	fun `conjured items degrade twice as fast`() {
		val hat = conjuredHat()
		cut = GildedRoseShop(hat)

		cut.updateItems()

		hat.sellIn shouldBe 0
		hat.quality shouldBe 4
	}

	@Test
	fun `conjured items age eve faster after the sell-by date`() {
		val hat = conjuredHat()
		cut = GildedRoseShop(hat)

		cut.updateItems(2)

		hat.sellIn shouldBe -1
		hat.quality shouldBe 0
	}

	@Test
	fun `backstage pass gains quality with time`() {
		val pass = backStagePass()
		cut = GildedRoseShop(pass)

		cut.updateItems()

		pass.sellIn shouldBe 11
		pass.quality shouldBe 13
	}

	@Test
	fun `backstage pass gains 2 points quality when there are 10 or less days left`() {
		val pass = backStagePass()
		cut = GildedRoseShop(pass)

		cut.updateItems(days = 2)

		pass.sellIn shouldBe 10
		pass.quality shouldBe 15
	}

	@Test
	fun `backstage pass gains 3 points quality when there are 5 or less days left`() {
		val pass = backStagePass()
		cut = GildedRoseShop(pass)

		cut.updateItems(days = 7)

		pass.sellIn shouldBe 5
		pass.quality shouldBe 26
	}
}

fun aSimpleStick() = Item("A Simple Stick", 1, 1)

fun bootsOfWaterWalking() = Item("Boots Of Water Walking", 2, 5)

fun agedBrie() = AgedBrie(1, 3)

fun backStagePass() = BackStagePass(12, 12)

fun laserPoweredGoblinSmasher() = Sulfuras(10, 10)

fun conjuredHat() = ConjuredItem("A Conjured Hat", 1, 6)
