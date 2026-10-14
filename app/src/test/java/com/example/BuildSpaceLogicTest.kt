package com.example

import com.example.data.model.BudgetTier
import com.example.data.model.InteriorStyle
import com.example.data.model.RegionPricing
import com.example.data.repository.BuildSpaceRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BuildSpaceLogicTest {

    private lateinit var repository: BuildSpaceRepository

    @Before
    fun setUp() {
        repository = BuildSpaceRepository()
    }

    @Test
    fun testCostEstimatorCalculatesAccurateTotalAndContingency() {
        val region = RegionPricing("NYC", "New York, USA", "$", 125.0, 1.25)
        val estimate = repository.calculateCostEstimate(
            lengthFt = 20.0,
            widthFt = 15.0,
            heightFt = 10.0,
            flooringType = "Engineered White Oak",
            wallFinish = "Venetian Plaster",
            fixturesQuality = "Premium Designer",
            region = region
        )

        // 20 x 15 = 300 sq ft floor
        assertEquals(300.0, estimate.roomAreaSqFt, 0.01)

        // Perimeter = 2*(20+15) = 70 ft, Wall Area = 70 * 10 = 700 sq ft
        assertEquals(700.0, estimate.wallAreaSqFt, 0.01)

        // Subtotal must be positive and contingency exactly 10% of subtotal
        assertTrue(estimate.subtotal > 0.0)
        assertEquals(estimate.subtotal * 0.10, estimate.contingency, 0.01)
        assertEquals(estimate.subtotal + estimate.contingency, estimate.grandTotal, 0.01)
    }

    @Test
    fun testAIDesignGenerationIncludesPaletteAndShoppingItems() {
        val design = repository.generateAIDesign(InteriorStyle.JAPANDI, BudgetTier.LUXURY)

        assertEquals(InteriorStyle.JAPANDI, design.style)
        assertEquals(BudgetTier.LUXURY, design.budgetTier)
        assertEquals(5, design.moodBoard.palette.size)
        assertTrue(design.moodBoard.materials.isNotEmpty())
        assertTrue(design.shoppingList.isNotEmpty())
        assertTrue(design.costEstimate > 0.0)
    }

    @Test
    fun testCollisionDetectionOnOverlappingFurniture() {
        // Place two furniture items overlapping at (10, 10)
        repository.addFurniture("Sofa A", 6.0f, 3.0f, 2.5f, "SEATING", "#D4B996")
        val itemsAfterFirst = repository.activeRoomFurniture.value
        val sofaA = itemsAfterFirst.last()

        // Move sofa A to (8f, 7f)
        repository.updateFurniturePosition(sofaA.id, 8f, 7f, 20f, 16f)

        // Place Sofa B at the exact same location
        repository.addFurniture("Sofa B", 6.0f, 3.0f, 2.5f, "SEATING", "#8C4A28")
        val itemsAfterSecond = repository.activeRoomFurniture.value
        val sofaB = itemsAfterSecond.last()
        repository.updateFurniturePosition(sofaB.id, 8f, 7f, 20f, 16f)

        val updatedItems = repository.activeRoomFurniture.value
        val updatedA = updatedItems.find { it.id == sofaA.id }
        val updatedB = updatedItems.find { it.id == sofaB.id }

        assertNotNull(updatedA)
        assertNotNull(updatedB)
        assertTrue("Sofa A should detect collision", updatedA!!.isColliding)
        assertTrue("Sofa B should detect collision", updatedB!!.isColliding)
    }
}
