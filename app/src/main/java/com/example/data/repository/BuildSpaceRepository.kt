package com.example.data.repository

import com.example.R
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class BuildSpaceRepository {

    private val _projects = MutableStateFlow<List<Project>>(createSeedProjects())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _selectedProjectId = MutableStateFlow<String>("proj_1")
    val selectedProjectId: StateFlow<String> = _selectedProjectId.asStateFlow()

    private val _contracts = MutableStateFlow<List<ContractRecord>>(createSeedContracts())
    val contracts: StateFlow<List<ContractRecord>> = _contracts.asStateFlow()

    private val _invoices = MutableStateFlow<List<InvoiceRecord>>(createSeedInvoices())
    val invoices: StateFlow<List<InvoiceRecord>> = _invoices.asStateFlow()

    private val _activeRoomFurniture = MutableStateFlow<List<FurnitureItem>>(createInitialFurniture())
    val activeRoomFurniture: StateFlow<List<FurnitureItem>> = _activeRoomFurniture.asStateFlow()

    fun selectProject(projectId: String) {
        _selectedProjectId.value = projectId
    }

    fun getSelectedProject(): Project {
        return _projects.value.firstOrNull { it.id == _selectedProjectId.value }
            ?: _projects.value.first()
    }

    fun updateProjectStatus(projectId: String, newStatus: ProjectStatus) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) proj.copy(status = newStatus) else proj
            }
        }
    }

    fun updateTaskStatus(projectId: String, taskId: String, newStatus: ProjectStatus) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val updatedTasks = proj.tasks.map { task ->
                        if (task.id == taskId) task.copy(status = newStatus) else task
                    }
                    proj.copy(tasks = updatedTasks)
                } else proj
            }
        }
    }

    fun addTask(projectId: String, title: String, status: ProjectStatus, assignedTo: String, dueDate: String, priority: String) {
        val newTask = TaskItem(
            id = "task_${UUID.randomUUID().toString().take(6)}",
            projectId = projectId,
            title = title,
            status = status,
            assignedTo = assignedTo,
            dueDate = dueDate,
            priority = priority
        )
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    proj.copy(tasks = proj.tasks + newTask)
                } else proj
            }
        }
    }

    fun addProject(name: String, type: ProjectType, clientName: String, location: String, totalBudget: Double) {
        val newProj = Project(
            id = "proj_${UUID.randomUUID().toString().take(6)}",
            name = name,
            type = type,
            status = ProjectStatus.PLANNING,
            clientName = clientName,
            location = location,
            totalBudget = totalBudget,
            spentBudget = 0.0,
            progressPercent = 10,
            rooms = listOf(
                Room(
                    id = "room_${UUID.randomUUID().toString().take(6)}",
                    projectId = "proj_new",
                    name = "Primary Living Space",
                    lengthFt = 18f,
                    widthFt = 15f,
                    heightFt = 9.5f
                )
            ),
            tasks = listOf(
                TaskItem("t1", "proj_new", "Initial Site Architectural Survey", ProjectStatus.PLANNING, "Alex Miller", "Nov 02", "High"),
                TaskItem("t2", "proj_new", "Concept Moodboards & Material Board", ProjectStatus.DESIGN, "Sofia Chen", "Nov 15", "Medium")
            ),
            budgetItems = listOf(
                BudgetItem("b1", "Architecture & Permits", totalBudget * 0.15, 0.0, "Permit filing & structural drawings"),
                BudgetItem("b2", "Finishes & Materials", totalBudget * 0.45, 0.0, "Hardwood, tile, fixtures"),
                BudgetItem("b3", "Labor & GC", totalBudget * 0.40, 0.0, "Contractor subcontracting")
            )
        )
        _projects.update { it + newProj }
        _selectedProjectId.value = newProj.id
    }

    fun toggleDesignApproval(projectId: String, roomId: String, designId: String) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val updatedRooms = proj.rooms.map { room ->
                        if (room.id == roomId) {
                            val updatedDesigns = room.designs.map { design ->
                                if (design.id == designId) design.copy(approved = !design.approved) else design
                            }
                            room.copy(designs = updatedDesigns)
                        } else room
                    }
                    proj.copy(rooms = updatedRooms)
                } else proj
            }
        }
    }

    fun addDesignPinComment(projectId: String, roomId: String, designId: String, x: Float, y: Float, text: String, author: String) {
        val newComment = DesignPinComment(
            id = "pin_${UUID.randomUUID().toString().take(6)}",
            xPercent = x,
            yPercent = y,
            author = author,
            role = "Client",
            text = text,
            timestamp = "Just now"
        )
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val updatedRooms = proj.rooms.map { room ->
                        if (room.id == roomId) {
                            val updatedDesigns = room.designs.map { design ->
                                if (design.id == designId) {
                                    design.copy(comments = design.comments + newComment)
                                } else design
                            }
                            room.copy(designs = updatedDesigns)
                        } else room
                    }
                    proj.copy(rooms = updatedRooms)
                } else proj
            }
        }
    }

    fun signContract(contractId: String, signer: String) {
        _contracts.update { list ->
            list.map { contract ->
                if (contract.id == contractId) {
                    contract.copy(
                        status = "SIGNED",
                        signedDate = "Oct 14, 2026",
                        signerName = signer
                    )
                } else contract
            }
        }
    }

    fun createInvoice(clientName: String, projectName: String, amount: Double, dueDate: String): InvoiceRecord {
        val invoice = InvoiceRecord(
            id = "inv_${UUID.randomUUID().toString().take(6)}",
            invoiceNumber = "INV-2026-${(100..999).random()}",
            clientName = clientName,
            projectName = projectName,
            amount = amount,
            dueDate = dueDate,
            status = "SENT"
        )
        _invoices.update { listOf(invoice) + it }
        return invoice
    }

    fun payInvoice(invoiceId: String) {
        _invoices.update { list ->
            list.map { if (it.id == invoiceId) it.copy(status = "PAID") else it }
        }
    }

    // 3D Room Planner operations & collision detection
    fun updateFurniturePosition(id: String, newX: Float, newY: Float, roomLength: Float, roomWidth: Float) {
        _activeRoomFurniture.update { list ->
            val updated = list.map { item ->
                if (item.id == id) {
                    // Clamp inside room bounds
                    val clampedX = newX.coerceIn(item.widthFt / 2f, roomLength - item.widthFt / 2f)
                    val clampedY = newY.coerceIn(item.depthFt / 2f, roomWidth - item.depthFt / 2f)
                    item.copy(xFt = clampedX, yFt = clampedY)
                } else item
            }
            detectCollisions(updated)
        }
    }

    fun rotateFurniture(id: String) {
        _activeRoomFurniture.update { list ->
            val updated = list.map { item ->
                if (item.id == id) {
                    val nextRot = (item.rotationDeg + 90f) % 360f
                    // swap width & depth for 90/270 deg
                    item.copy(rotationDeg = nextRot)
                } else item
            }
            detectCollisions(updated)
        }
    }

    fun addFurniture(name: String, widthFt: Float, depthFt: Float, heightFt: Float, category: String, colorHex: String) {
        val newItem = FurnitureItem(
            id = "furn_${UUID.randomUUID().toString().take(6)}",
            name = name,
            widthFt = widthFt,
            depthFt = depthFt,
            heightFt = heightFt,
            xFt = 8f,
            yFt = 7f,
            category = category,
            colorHex = colorHex
        )
        _activeRoomFurniture.update { detectCollisions(it + newItem) }
    }

    fun removeFurniture(id: String) {
        _activeRoomFurniture.update { list ->
            detectCollisions(list.filterNot { it.id == id })
        }
    }

    fun resetFurnitureLayout() {
        _activeRoomFurniture.value = detectCollisions(createInitialFurniture())
    }

    private fun detectCollisions(items: List<FurnitureItem>): List<FurnitureItem> {
        val collidingIds = mutableSetOf<String>()
        for (i in items.indices) {
            val a = items[i]
            // Skip doors and windows on walls from floor collision
            if (a.category == "DOOR" || a.category == "WINDOW") continue

            val aMinX = a.xFt - a.widthFt / 2f
            val aMaxX = a.xFt + a.widthFt / 2f
            val aMinY = a.yFt - a.depthFt / 2f
            val aMaxY = a.yFt + a.depthFt / 2f

            for (j in i + 1 until items.size) {
                val b = items[j]
                if (b.category == "DOOR" || b.category == "WINDOW") continue

                val bMinX = b.xFt - b.widthFt / 2f
                val bMaxX = b.xFt + b.widthFt / 2f
                val bMinY = b.yFt - b.depthFt / 2f
                val bMaxY = b.yFt + b.depthFt / 2f

                val overlapX = aMinX < bMaxX && aMaxX > bMinX
                val overlapY = aMinY < bMaxY && aMaxY > bMinY

                if (overlapX && overlapY) {
                    collidingIds.add(a.id)
                    collidingIds.add(b.id)
                }
            }
        }
        return items.map { item ->
            item.copy(isColliding = collidingIds.contains(item.id))
        }
    }

    // AI Interior Designer generator
    fun generateAIDesign(style: InteriorStyle, tier: BudgetTier): DesignVariant {
        val (palette, materials, textures, rationale) = when (style) {
            InteriorStyle.JAPANDI -> Quadruple(
                listOf(
                    MoodColor("Wabi Sand", "#EADEC9"),
                    MoodColor("White Oak", "#D4B996"),
                    MoodColor("Charcoal Ink", "#2B2D2F"),
                    MoodColor("Matcha Ash", "#9A9E8B"),
                    MoodColor("Warm Linen", "#F6F4EE")
                ),
                listOf("Solid White Oak", "Travertine Stone", "Rice Paper Shoji"),
                listOf("Raw Bouclé", "Woven Jute", "Fluted Wood Slats"),
                "A balanced harmony of Scandinavian comfort and Japanese architectural minimalism, promoting tranquility through low-slung profiles, soft organic textures, and warm natural illumination."
            )
            InteriorStyle.MODERN -> Quadruple(
                listOf(
                    MoodColor("Crisp Alabaster", "#F8FAFC"),
                    MoodColor("Architectural Slate", "#1E293B"),
                    MoodColor("Brushed Brass", "#C99A45"),
                    MoodColor("Warm Cognac", "#8C4A28"),
                    MoodColor("Monolith Gray", "#64748B")
                ),
                listOf("Blackened Steel", "Calacatta Marble", "Smoked Walnut"),
                listOf("Full-Grain Italian Leather", "Ribbed Glass", "Micro-Cement"),
                "Precision-engineered spatial layout emphasizing continuous sightlines, monolithic surfaces, integrated architectural lighting, and bespoke statement furniture."
            )
            InteriorStyle.SCANDINAVIAN -> Quadruple(
                listOf(
                    MoodColor("Stockholm White", "#FAF9F6"),
                    MoodColor("Nordic Birch", "#E6D5B8"),
                    MoodColor("Fjord Mist", "#CBD5E1"),
                    MoodColor("Sage Spruce", "#84A98C"),
                    MoodColor("Warm Clay", "#D8A47F")
                ),
                listOf("Pale Ash Wood", "Ceramic Tile", "Cast Iron"),
                listOf("Chunky Wool Knit", "Brushed Cotton Canvas", "Natural Rattan"),
                "Bright, airy layout maximizing natural daylight bounce with comfortable ergonomic seating and warm hygge layering."
            )
            InteriorStyle.INDUSTRIAL -> Quadruple(
                listOf(
                    MoodColor("Cast Iron", "#18181B"),
                    MoodColor("Exposed Brick", "#9A3412"),
                    MoodColor("Raw Cement", "#71717A"),
                    MoodColor("Oxidized Bronze", "#78350F"),
                    MoodColor("Distressed Tan", "#A16207")
                ),
                listOf("Structural I-Beams", "Polished Concrete", "Reclaimed Barnwood"),
                listOf("Weathered Leather", "Perforated Mesh", "Rough-Sawn Timber"),
                "Celebrates raw structural honesty with high ceilings, track spotlights, ductwork accents, and durable high-tactility materials."
            )
            else -> Quadruple(
                listOf(
                    MoodColor("Warm Greige", "#E2DCD5"),
                    MoodColor("Earthy Terracotta", "#C26D53"),
                    MoodColor("Deep Olive", "#47553A"),
                    MoodColor("Soft Cream", "#F7F5F0"),
                    MoodColor("Muted Charcoal", "#333A42")
                ),
                listOf("Honed Limestone", "Warm Walnut", "Brushed Nickel"),
                listOf("Woven Linen", "Matte Ceramic", "Textured Plaster"),
                "A curated, timeless aesthetic combining organic warmth, balanced proportions, and functional elegance tailored to contemporary living."
            )
        }

        val priceFactor = tier.multiplier
        val shoppingList = listOf(
            ShoppingItem("s1", "Modular Curved Bouclé Sectional", "Seating", 1850.0 * priceFactor, "Wayfair / Custom Studio", "boucle-curved-sectional"),
            ShoppingItem("s2", "Organic Travertine Low Coffee Table", "Tables", 620.0 * priceFactor, "CB2 / West Elm", "travertine-round-table"),
            ShoppingItem("s3", "Sculptural Cast Plaster Floor Lamp", "Lighting", 380.0 * priceFactor, "Arteriors / Lumens", "plaster-sculptural-lamp"),
            ShoppingItem("s4", "Hand-Knotted High-Low Wool Rug (9x12)", "Rugs", 890.0 * priceFactor, "Nordic Knots / Pottery Barn", "wool-hand-knotted-rug"),
            ShoppingItem("s5", "White Oak Slatted Media Credenza", "Storage", 1150.0 * priceFactor, "IKEA Stockholm / Article", "oak-slat-credenza"),
            ShoppingItem("s6", "Oversized Minimalist Gallery Art Frame", "Decor", 240.0 * priceFactor, "Amazon / Crate & Barrel", "gallery-oak-frame-large")
        )

        return DesignVariant(
            id = "des_${UUID.randomUUID().toString().take(6)}",
            roomId = "room_1",
            style = style,
            budgetTier = tier,
            beforeImageRes = R.drawable.img_room_before,
            afterImageRes = R.drawable.img_room_after,
            moodBoard = MoodBoard(palette, materials, textures, rationale),
            shoppingList = shoppingList,
            costEstimate = shoppingList.sumOf { it.price } + 3500.0 * priceFactor,
            version = 1,
            approved = false,
            comments = listOf(
                DesignPinComment(
                    id = "pin_init",
                    xPercent = 0.52f,
                    yPercent = 0.65f,
                    author = "Elena Vance (Client)",
                    role = "Client",
                    text = "Love the curved bouclé profile! Can we make sure the fabric is stain-resistant performance weave?",
                    timestamp = "Yesterday"
                )
            )
        )
    }

    // Cost Estimator calculation
    fun calculateCostEstimate(
        lengthFt: Double,
        widthFt: Double,
        heightFt: Double,
        flooringType: String,
        wallFinish: String,
        fixturesQuality: String,
        region: RegionPricing
    ): CostEstimate {
        val floorArea = lengthFt * widthFt
        val perimeter = 2 * (lengthFt + widthFt)
        val wallArea = perimeter * heightFt

        val flooringPricePerSqFt = when (flooringType) {
            "Italian Porcelain Tile" -> 16.50
            "Engineered White Oak" -> 14.00
            "Solid Hardwood Herringbone" -> 22.00
            "Luxury Vinyl Plank (LVP)" -> 6.50
            else -> 12.00
        } * region.materialMultiplier

        val wallPricePerSqFt = when (wallFinish) {
            "Venetian Plaster" -> 18.00
            "Architectural Matte Paint (2 Coats)" -> 3.75
            "Textured Grasscloth Wallpaper" -> 9.50
            "Acoustic Wood Slat Paneling" -> 24.00
            else -> 4.50
        } * region.materialMultiplier

        val fixturesPrice = when (fixturesQuality) {
            "Luxury Architectural" -> 5800.0
            "Premium Designer" -> 3200.0
            else -> 1500.0
        } * region.materialMultiplier

        val flooringTotal = floorArea * flooringPricePerSqFt
        val wallTotal = wallArea * wallPricePerSqFt
        val baseboardTotal = perimeter * 8.50 * region.materialMultiplier

        // Labor calculation
        val laborHours = (floorArea * 0.18) + (wallArea * 0.12) + 24.0
        val laborCost = laborHours * region.laborRatePerHour

        val items = listOf(
            EstimateItem("Flooring Supply ($flooringType)", "Materials", floorArea, "sq ft", flooringPricePerSqFt, flooringTotal),
            EstimateItem("Wall Finishes ($wallFinish)", "Materials", wallArea, "sq ft", wallPricePerSqFt, wallTotal),
            EstimateItem("Architectural Baseboards & Trim", "Materials", perimeter, "linear ft", 8.50 * region.materialMultiplier, baseboardTotal),
            EstimateItem("Lighting & Electrical Fixtures", "Fixtures", 1.0, "pkg", fixturesPrice, fixturesPrice),
            EstimateItem("Skilled Trade Labor (Tile & Carpentry)", "Labor", laborHours, "hours", region.laborRatePerHour, laborCost)
        )

        val subtotal = items.sumOf { it.total }
        val contingency = subtotal * 0.10 // 10% contingency
        val grandTotal = subtotal + contingency

        return CostEstimate(
            roomAreaSqFt = floorArea,
            wallAreaSqFt = wallArea,
            region = region,
            items = items,
            subtotal = subtotal,
            laborCost = laborCost,
            contingency = contingency,
            grandTotal = grandTotal
        )
    }

    private fun createSeedProjects(): List<Project> {
        val proj1 = Project(
            id = "proj_1",
            name = "Skyline Penthouse Redesign",
            type = ProjectType.INTERIOR_ONLY,
            status = ProjectStatus.DESIGN,
            clientName = "Elena Vance",
            location = "Tribeca, New York",
            totalBudget = 145000.0,
            spentBudget = 42300.0,
            progressPercent = 45,
            rooms = listOf(
                Room(
                    id = "room_1",
                    projectId = "proj_1",
                    name = "Primary Salon & Living Lounge",
                    lengthFt = 22f,
                    widthFt = 16f,
                    heightFt = 10.5f,
                    designs = listOf(
                        DesignVariant(
                            id = "des_1",
                            roomId = "room_1",
                            style = InteriorStyle.JAPANDI,
                            budgetTier = BudgetTier.LUXURY,
                            beforeImageRes = R.drawable.img_room_before,
                            afterImageRes = R.drawable.img_room_after,
                            moodBoard = MoodBoard(
                                palette = listOf(
                                    MoodColor("Wabi Sand", "#EADEC9"),
                                    MoodColor("White Oak", "#D4B996"),
                                    MoodColor("Charcoal Ink", "#2B2D2F"),
                                    MoodColor("Matcha Ash", "#9A9E8B"),
                                    MoodColor("Warm Linen", "#F6F4EE")
                                ),
                                materials = listOf("Solid White Oak", "Travertine Stone", "Rice Paper Shoji"),
                                textures = listOf("Raw Bouclé", "Woven Jute", "Fluted Wood Slats"),
                                designRationale = "A serene sanctuary combining Scandinavian warmth and Japanese simplicity. Accented by low floor profiles and natural light."
                            ),
                            shoppingList = listOf(
                                ShoppingItem("s1", "Bespoke Curved Bouclé Sectional", "Seating", 3400.0, "Custom Studio NY", "curved-boucle-sofa"),
                                ShoppingItem("s2", "Honed Travertine Monolith Table", "Tables", 1250.0, "CB2 Collection", "honed-travertine-table"),
                                ShoppingItem("s3", "Woven Paper Rope Easy Chair", "Seating", 820.0, "Carl Hansen", "ch25-easy-chair"),
                                ShoppingItem("s4", "Linear Fluted Oak Media Console", "Storage", 2100.0, "Muuto Design", "fluted-oak-console"),
                                ShoppingItem("s5", "Akari Japanese Paper Pendant Light", "Lighting", 480.0, "Vitra / Noguchi", "akari-paper-pendant"),
                                ShoppingItem("s6", "Oversized Minimalist Ceramic Floor Vessel", "Decor", 320.0, "Ferm Living", "ceramic-floor-vase")
                            ),
                            costEstimate = 32800.0,
                            version = 1,
                            approved = false,
                            comments = listOf(
                                DesignPinComment("pin_1", 0.52f, 0.65f, "Elena Vance", "Client", "Let's ensure the bouclé is treated with Crypton stain shield.", "2h ago"),
                                DesignPinComment("pin_2", 0.25f, 0.35f, "Marc Architect", "Designer", "We placed the Akari pendant at 7ft clearance over the low coffee table.", "1h ago")
                            )
                        )
                    )
                ),
                Room(
                    id = "room_2",
                    projectId = "proj_1",
                    name = "Master Suite & Private Terrace",
                    lengthFt = 18f,
                    widthFt = 14f,
                    heightFt = 9.5f
                )
            ),
            tasks = listOf(
                TaskItem("t_101", "proj_1", "Finalize Material Sample Board Approval", ProjectStatus.DESIGN, "Sofia Chen", "Oct 18", "High"),
                TaskItem("t_102", "proj_1", "Submit Electrical Layout for Recessed Cove", ProjectStatus.PERMITS, "David Ross", "Oct 24", "High", "t_101"),
                TaskItem("t_103", "proj_1", "Order Custom Bouclé Sectional & Rug", ProjectStatus.CONSTRUCTION, "Elena Vance", "Nov 02", "Medium"),
                TaskItem("t_104", "proj_1", "Subfloor Leveling & Herringbone Oak Install", ProjectStatus.CONSTRUCTION, "Apex Flooring LLC", "Nov 12", "High"),
                TaskItem("t_105", "proj_1", "Paint Finishes & Venetian Plaster Accent", ProjectStatus.FINISHING, "Studio Finish Co.", "Nov 25", "Medium"),
                TaskItem("t_106", "proj_1", "Architectural Sign-off & Client Punch List", ProjectStatus.DONE, "Alex Miller", "Dec 05", "Low")
            ),
            budgetItems = listOf(
                BudgetItem("b_1", "Custom Furniture & Joinery", 52000.0, 18500.0, "Sectional, credenza, bespoke vanity"),
                BudgetItem("b_2", "Hard Finishes & Flooring", 38000.0, 14200.0, "White oak herringbone & travertine"),
                BudgetItem("b_3", "Lighting & Electrical Fixtures", 18000.0, 4800.0, "Akari pendants, Lutron dimmers"),
                BudgetItem("b_4", "Trade Contractor Labor", 37000.0, 4800.0, "Carpentry, paint, electrical")
            ),
            documents = listOf(
                DocumentItem("d_1", "proj_1", "BuildSpace Client Master Agreement.pdf", "CONTRACT", "2.4 MB", "Oct 10, 2026", "Pending Sign"),
                DocumentItem("d_2", "proj_1", "Penthouse Level 42 Architectural Blueprint.dwg", "BLUEPRINT", "18.6 MB", "Oct 08, 2026", "Approved"),
                DocumentItem("d_3", "proj_1", "NYC Dept of Buildings Alteration Permit #8291.pdf", "PERMIT", "1.1 MB", "Oct 12, 2026", "Active"),
                DocumentItem("d_4", "proj_1", "Phase 1 Retainer Invoice #INV-2026-442.pdf", "INVOICE", "640 KB", "Oct 05, 2026", "Approved")
            )
        )

        val proj2 = Project(
            id = "proj_2",
            name = "Lakeside Contemporary Residence",
            type = ProjectType.RESIDENTIAL,
            status = ProjectStatus.CONSTRUCTION,
            clientName = "Marcus Sterling",
            location = "Lake Tahoe, California",
            totalBudget = 420000.0,
            spentBudget = 195000.0,
            progressPercent = 68,
            rooms = listOf(
                Room("room_201", "proj_2", "Great Room & Open Kitchen", 28f, 20f, 14f),
                Room("room_202", "proj_2", "Cantilevered Deck Lounge", 22f, 12f, 10f)
            ),
            tasks = listOf(
                TaskItem("t_201", "proj_2", "Foundation Seismic Inspection", ProjectStatus.PERMITS, "Tahoe County Insp.", "Aug 15", "High"),
                TaskItem("t_202", "proj_2", "Glulam Timber Framing & Steel Trusses", ProjectStatus.CONSTRUCTION, "Sierra Builders", "Sep 10", "High"),
                TaskItem("t_203", "proj_2", "Triple-Pane Curtain Wall Glazing", ProjectStatus.CONSTRUCTION, "Tahoe Glass", "Oct 20", "High"),
                TaskItem("t_204", "proj_2", "Hydronic Radiant Floor PEX Tubing", ProjectStatus.CONSTRUCTION, "Alpine Mechanical", "Nov 05", "Medium"),
                TaskItem("t_205", "proj_2", "Cabinetry Millwork Install", ProjectStatus.FINISHING, "Custom Studio", "Dec 01", "Medium")
            ),
            budgetItems = listOf(
                BudgetItem("b_201", "Structural Steel & Timber", 120000.0, 118000.0, "Completed framing"),
                BudgetItem("b_202", "Curtain Wall & Windows", 85000.0, 42000.0, "Deposit paid"),
                BudgetItem("b_203", "Interior Millwork & Kitchen", 95000.0, 15000.0, "Shop fabrication"),
                BudgetItem("b_204", "Mechanical, Electrical, Plumbing", 70000.0, 20000.0, "Rough-in ongoing"),
                BudgetItem("b_205", "Contingency Fund", 50000.0, 0.0, "Unused")
            ),
            documents = listOf(
                DocumentItem("d_201", "proj_2", "Sierra Master Contractor Contract.pdf", "CONTRACT", "4.1 MB", "Aug 01, 2026", "Approved"),
                DocumentItem("d_202", "proj_2", "Structural Engineering Blueprints Rev4.pdf", "BLUEPRINT", "32.0 MB", "Aug 12, 2026", "Approved")
            )
        )

        return listOf(proj1, proj2)
    }

    private fun createSeedContracts(): List<ContractRecord> {
        return listOf(
            ContractRecord(
                id = "ctr_1",
                projectId = "proj_1",
                title = "BuildSpace Master Design & Construction Services Agreement",
                clientName = "Elena Vance",
                totalFee = 145000.0,
                status = "PENDING"
            ),
            ContractRecord(
                id = "ctr_2",
                projectId = "proj_2",
                title = "General Contractor Prime Contract & Milestone Schedule",
                clientName = "Marcus Sterling",
                totalFee = 420000.0,
                status = "SIGNED",
                signedDate = "Aug 02, 2026",
                signerName = "Marcus Sterling"
            )
        )
    }

    private fun createSeedInvoices(): List<InvoiceRecord> {
        return listOf(
            InvoiceRecord("inv_1", "INV-2026-442", "Elena Vance", "Skyline Penthouse Redesign", 25000.0, "Oct 20, 2026", "SENT"),
            InvoiceRecord("inv_2", "INV-2026-381", "Elena Vance", "Skyline Penthouse Redesign", 17300.0, "Sep 15, 2026", "PAID"),
            InvoiceRecord("inv_3", "INV-2026-210", "Marcus Sterling", "Lakeside Contemporary Residence", 85000.0, "Oct 01, 2026", "PAID")
        )
    }

    private fun createInitialFurniture(): List<FurnitureItem> {
        return listOf(
            FurnitureItem("f_sofa", "Bouclé Curved Sofa", 7.5f, 3.5f, 2.8f, 10f, 6f, 0f, "SEATING", "#D4B996"),
            FurnitureItem("f_table", "Travertine Low Table", 3.2f, 3.2f, 1.4f, 10f, 10.5f, 0f, "TABLE", "#EADEC9"),
            FurnitureItem("f_chair1", "Ash Lounge Chair", 2.6f, 2.8f, 2.7f, 6.2f, 10.5f, 45f, "SEATING", "#9A9E8B"),
            FurnitureItem("f_credenza", "Slatted Media Console", 6.0f, 1.6f, 2.0f, 10f, 14.5f, 0f, "STORAGE", "#78350F"),
            FurnitureItem("f_door", "Main Entry Pivot Door", 3.2f, 0.5f, 8.0f, 2.5f, 0.25f, 0f, "DOOR", "#0284C7"),
            FurnitureItem("f_window", "Panoramic Floor Window", 8.0f, 0.4f, 7.5f, 10f, 0.2f, 0f, "WINDOW", "#38BDF8")
        )
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
