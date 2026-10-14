package com.example.data.model

enum class ProjectType(val label: String) {
    RESIDENTIAL("Residential"),
    COMMERCIAL("Commercial"),
    INTERIOR_ONLY("Interior Only")
}

enum class ProjectStatus(val label: String, val stepOrder: Int) {
    PLANNING("Planning", 1),
    DESIGN("Design", 2),
    PERMITS("Permits", 3),
    CONSTRUCTION("Construction", 4),
    FINISHING("Finishing", 5),
    DONE("Done", 6)
}

enum class InteriorStyle(val displayName: String, val description: String) {
    JAPANDI("Japandi", "Minimalist harmony blending Scandinavian hygge & Japanese wabi-sabi"),
    MODERN("Modern", "Crisp lines, bold geometric volumes, and understated luxury"),
    SCANDINAVIAN("Scandinavian", "Light woods, neutral soft palettes, airy warmth"),
    INDUSTRIAL("Industrial", "Exposed concrete, structural steel, reclaimed timbers"),
    MINIMALIST("Minimalist", "Pure geometries, concealed storage, calm spatial purity"),
    BOHEMIAN("Bohemian", "Rich woven textures, earthy terracottas, curated eclectic accents"),
    ART_DECO("Art Deco", "Gilded brass inlays, fluted millwork, geometric glamor"),
    RUSTIC("Rustic", "Organic stone, rugged raw timber beams, artisanal honesty"),
    COASTAL("Coastal", "Sun-bleached linens, ocean breezes, organic driftwood"),
    CONTEMPORARY("Contemporary", "Curved fluid profiles, mixed materiality, future-forward")
}

enum class BudgetTier(val label: String, val multiplier: Double) {
    ECONOMY("Economy Tier", 0.75),
    MID_RANGE("Mid-Range Tier", 1.0),
    LUXURY("Luxury Studio Tier", 1.85)
}

data class MoodColor(
    val name: String,
    val hex: String
)

data class MoodBoard(
    val palette: List<MoodColor>,
    val materials: List<String>,
    val textures: List<String>,
    val designRationale: String
)

data class ShoppingItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val store: String,
    val searchKeyword: String
)

data class DesignPinComment(
    val id: String,
    val xPercent: Float,
    val yPercent: Float,
    val author: String,
    val role: String,
    val text: String,
    val timestamp: String,
    val resolved: Boolean = false
)

data class DesignVariant(
    val id: String,
    val roomId: String,
    val style: InteriorStyle,
    val budgetTier: BudgetTier,
    val beforeImageRes: Int? = null,
    val afterImageRes: Int? = null,
    val moodBoard: MoodBoard,
    val shoppingList: List<ShoppingItem>,
    val costEstimate: Double,
    val version: Int = 1,
    val approved: Boolean = false,
    val comments: List<DesignPinComment> = emptyList()
)

data class FurnitureItem(
    val id: String,
    val name: String,
    val widthFt: Float,
    val depthFt: Float,
    val heightFt: Float,
    val xFt: Float,
    val yFt: Float,
    val rotationDeg: Float = 0f,
    val category: String, // "SEATING", "TABLE", "BED", "STORAGE", "DOOR", "WINDOW"
    val colorHex: String,
    val isColliding: Boolean = false
)

data class Room(
    val id: String,
    val projectId: String,
    val name: String,
    val lengthFt: Float = 16f,
    val widthFt: Float = 14f,
    val heightFt: Float = 9.5f,
    val designs: List<DesignVariant> = emptyList(),
    val furniture: List<FurnitureItem> = emptyList()
)

data class TaskItem(
    val id: String,
    val projectId: String,
    val title: String,
    val status: ProjectStatus,
    val assignedTo: String,
    val dueDate: String,
    val priority: String = "Medium", // High, Medium, Low
    val dependency: String? = null
)

data class BudgetItem(
    val id: String,
    val category: String,
    val estimatedAmount: Double,
    val actualAmount: Double,
    val notes: String
)

data class DocumentItem(
    val id: String,
    val projectId: String,
    val title: String,
    val type: String, // "CONTRACT", "BLUEPRINT", "PERMIT", "INVOICE"
    val fileSize: String,
    val dateModified: String,
    val status: String // "Approved", "Pending Sign", "Active"
)

data class Project(
    val id: String,
    val name: String,
    val type: ProjectType,
    val status: ProjectStatus,
    val clientName: String,
    val location: String,
    val totalBudget: Double,
    val spentBudget: Double,
    val progressPercent: Int,
    val rooms: List<Room> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val budgetItems: List<BudgetItem> = emptyList(),
    val documents: List<DocumentItem> = emptyList(),
    val createdAt: String = "Oct 2026"
)

data class RegionPricing(
    val code: String,
    val name: String,
    val currency: String,
    val laborRatePerHour: Double,
    val materialMultiplier: Double
)

data class EstimateItem(
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val total: Double
)

data class CostEstimate(
    val roomAreaSqFt: Double,
    val wallAreaSqFt: Double,
    val region: RegionPricing,
    val items: List<EstimateItem>,
    val subtotal: Double,
    val laborCost: Double,
    val contingency: Double,
    val grandTotal: Double
)

data class ContractRecord(
    val id: String,
    val projectId: String,
    val title: String,
    val clientName: String,
    val totalFee: Double,
    val status: String, // "PENDING", "SIGNED"
    val signedDate: String? = null,
    val signerName: String? = null
)

data class InvoiceRecord(
    val id: String,
    val invoiceNumber: String,
    val clientName: String,
    val projectName: String,
    val amount: Double,
    val dueDate: String,
    val status: String // "PAID", "SENT", "OVERDUE"
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val suggestedActions: List<String> = emptyList()
)
