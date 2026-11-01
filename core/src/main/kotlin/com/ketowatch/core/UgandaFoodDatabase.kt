package com.ketowatch.core

import com.ketowatch.core.FoodCategory.*

/**
 * Built-in reference list of Ugandan dishes and foods commonly sold in Ugandan
 * supermarkets, markets and shops. Values are per typical serving and are
 * approximations based on standard food-composition data for the main
 * ingredients; home recipes vary.
 *
 * Used (1) as offline manual entry, (2) as reference context for the AI
 * recogniser so its estimates line up with how Ugandan food is actually served.
 */
object UgandaFoodDatabase {

    private fun f(
        id: String, name: String, local: List<String>, cat: FoodCategory, serving: String, grams: Int,
        kcal: Double, carbs: Double, fiber: Double, protein: Double, fat: Double,
    ) = FoodItem(id, name, local, cat, serving, grams, Nutrition(kcal, carbs, fiber, protein, fat))

    val foods: List<FoodItem> = listOf(
        // ---- Staples ----
        f("matooke", "Matooke (steamed green bananas)", listOf("Matooke", "Amatooke", "Ebitooke"), STAPLE, "1 plate, mashed", 250, 290.0, 77.0, 5.8, 3.0, 0.5),
        f("posho", "Posho (maize meal)", listOf("Kawunga", "Ugali", "Kwon"), STAPLE, "1 portion", 250, 300.0, 66.0, 3.5, 6.5, 1.3),
        f("millet_bread", "Millet bread", listOf("Kalo", "Atapa", "Kwon kal"), STAPLE, "1 portion", 250, 280.0, 60.0, 4.0, 6.0, 2.0),
        f("cassava", "Cassava, boiled", listOf("Muwogo", "Gwana"), STAPLE, "2 medium pieces", 200, 320.0, 76.0, 3.6, 2.7, 0.6),
        f("sweet_potato", "Sweet potatoes, boiled", listOf("Lumonde", "Lumonde lw'okufumba"), STAPLE, "2 medium", 200, 172.0, 40.0, 6.0, 3.2, 0.2),
        f("irish_potato", "Irish potatoes, boiled", listOf("Lumonde lwa Bazungu", "Irish"), STAPLE, "2 medium", 200, 174.0, 40.0, 3.6, 3.8, 0.2),
        f("rice", "White rice, cooked", listOf("Muceere"), STAPLE, "1 cup", 200, 260.0, 56.0, 0.8, 5.4, 0.6),
        f("pilau", "Pilau rice", listOf("Pilawo"), STAPLE, "1 plate", 250, 420.0, 62.0, 2.0, 14.0, 13.0),
        f("chapati", "Chapati", listOf("Chapo"), STAPLE, "1 chapati", 80, 260.0, 38.0, 2.0, 6.0, 9.0),
        f("gonja", "Roasted plantain", listOf("Gonja"), STAPLE, "1 large", 150, 180.0, 47.0, 3.4, 1.9, 0.6),
        f("yam", "Yam, boiled", listOf("Bikata", "Kyetutumula"), STAPLE, "2 pieces", 200, 236.0, 56.0, 8.0, 3.0, 0.3),
        f("katogo_offal", "Katogo (matooke with offal)", listOf("Katogo"), STAPLE, "1 plate", 350, 450.0, 75.0, 8.0, 18.0, 10.0),
        f("katogo_beans", "Katogo (matooke with beans)", listOf("Katogo ka bijanjaalo"), STAPLE, "1 plate", 350, 430.0, 82.0, 14.0, 14.0, 5.0),
        f("kikomando", "Kikomando (chapati with beans)", listOf("Kikomando", "Kiko"), STREET_FOOD, "1 plate", 350, 600.0, 85.0, 14.0, 20.0, 20.0),
        f("rolex", "Rolex (chapati rolled with eggs)", listOf("Rolex", "Rolled eggs"), STREET_FOOD, "1 rolex (2 eggs)", 250, 520.0, 42.0, 3.0, 18.0, 31.0),
        f("chips", "Chips (French fries)", listOf("Chips"), STREET_FOOD, "1 portion", 150, 470.0, 60.0, 5.0, 6.0, 23.0),
        f("chips_kuku", "Chips and chicken", listOf("Chips kuku"), STREET_FOOD, "1 portion", 350, 900.0, 62.0, 5.0, 45.0, 52.0),
        f("mandazi", "Mandazi", listOf("Mandazi", "Maandazi"), STREET_FOOD, "1 piece", 60, 220.0, 28.0, 1.0, 4.0, 10.0),
        f("kabalagala", "Kabalagala (banana pancakes)", listOf("Kabalagala"), STREET_FOOD, "2 pieces", 80, 260.0, 40.0, 2.0, 4.0, 10.0),
        f("samosa", "Samosa (meat)", listOf("Sambusa"), STREET_FOOD, "1 piece", 70, 210.0, 20.0, 1.5, 7.0, 12.0),
        f("roast_maize", "Roasted maize cob", listOf("Kasooli", "Roast maize"), STREET_FOOD, "1 cob", 150, 180.0, 38.0, 4.0, 5.0, 2.0),
        f("groundnuts", "Roasted groundnuts", listOf("Binyebwa ebikalange", "Ebinyobwa"), STREET_FOOD, "1 small packet", 50, 290.0, 8.0, 4.3, 13.0, 25.0),
        f("nsenene", "Fried grasshoppers", listOf("Nsenene"), STREET_FOOD, "1 cup", 100, 320.0, 6.0, 4.0, 25.0, 22.0),
        f("white_ants", "White ants (termites), fried", listOf("Nswa"), STREET_FOOD, "1 cup", 80, 290.0, 4.0, 2.0, 15.0, 24.0),
        f("muchomo", "Muchomo (grilled meat skewers)", listOf("Muchomo", "Mchomo", "Nyama choma"), STREET_FOOD, "3 skewers", 150, 300.0, 2.0, 0.0, 33.0, 18.0),
        f("chicken_muchomo", "Grilled chicken (roadside)", listOf("Kuku muchomo", "Enkoko enjokye"), STREET_FOOD, "1 quarter", 200, 380.0, 1.0, 0.0, 44.0, 22.0),
        f("pork_roast", "Roast pork (pork joint)", listOf("Pork joint", "Embizzi"), MEAT_FISH, "1 plate", 200, 520.0, 2.0, 0.0, 40.0, 40.0),

        // ---- Sauces & stews ----
        f("luwombo_chicken", "Chicken luwombo", listOf("Luwombo lw'enkoko"), SAUCE_STEW, "1 parcel", 350, 450.0, 10.0, 3.0, 40.0, 28.0),
        f("luwombo_beef", "Beef luwombo", listOf("Luwombo lw'ennyama"), SAUCE_STEW, "1 parcel", 350, 480.0, 8.0, 2.0, 42.0, 30.0),
        f("groundnut_sauce", "Groundnut sauce", listOf("Binyebwa", "Ebinyebwa", "Odii"), SAUCE_STEW, "1 cup", 200, 300.0, 14.0, 5.0, 13.0, 23.0),
        f("beans", "Beans stew", listOf("Bijanjaalo", "Ebijanjaalo"), SAUCE_STEW, "1 cup", 250, 280.0, 45.0, 15.0, 17.0, 3.5),
        f("peas", "Peas stew (dried peas)", listOf("Mbaazi", "Pigeon peas"), SAUCE_STEW, "1 cup", 250, 270.0, 44.0, 14.0, 16.0, 3.0),
        f("malakwang", "Malakwang (sour leaves in groundnut paste)", listOf("Malakwang"), SAUCE_STEW, "1 cup", 200, 200.0, 10.0, 5.0, 8.0, 15.0),
        f("boo", "Boo (cowpea leaves in simsim)", listOf("Boo", "Eboo"), SAUCE_STEW, "1 cup", 200, 180.0, 10.0, 6.0, 8.0, 13.0),
        f("entula", "Garden eggs stew", listOf("Entula", "Biringanya"), SAUCE_STEW, "1 cup", 200, 120.0, 12.0, 6.0, 2.0, 8.0),
        f("beef_stew", "Beef stew", listOf("Ennyama", "Nyama"), SAUCE_STEW, "1 cup", 250, 380.0, 8.0, 1.5, 38.0, 22.0),
        f("goat_stew", "Goat stew", listOf("Embuzi", "Mbuzi"), SAUCE_STEW, "1 cup", 250, 330.0, 5.0, 1.0, 40.0, 16.0),
        f("chicken_stew", "Chicken stew", listOf("Enkoko", "Kuku"), SAUCE_STEW, "1 cup", 250, 380.0, 7.0, 1.5, 36.0, 23.0),
        f("offal_stew", "Offal stew (tripe & intestines)", listOf("Byenda", "Mulokony"), SAUCE_STEW, "1 cup", 250, 300.0, 4.0, 0.5, 30.0, 18.0),
        f("fish_stew", "Tilapia stew", listOf("Ngege mu supu", "Ngege"), SAUCE_STEW, "1 fish with soup", 300, 330.0, 6.0, 1.0, 45.0, 14.0),
        f("mukene", "Silverfish (mukene) with tomato & onion", listOf("Mukene", "Omena"), SAUCE_STEW, "1 cup", 200, 250.0, 8.0, 2.0, 30.0, 11.0),
        f("simsim_paste", "Simsim (sesame) paste", listOf("Simsim", "Nnyaanya ya simsim"), SAUCE_STEW, "2 tablespoons", 30, 180.0, 7.0, 3.0, 5.0, 16.0),

        // ---- Meat, fish & eggs ----
        f("fried_tilapia", "Fried whole tilapia", listOf("Ngege enjokye", "Fried fish"), MEAT_FISH, "1 medium fish", 300, 450.0, 4.0, 0.0, 52.0, 26.0),
        f("nile_perch", "Nile perch, grilled", listOf("Empuuta", "Mputa"), MEAT_FISH, "1 fillet", 200, 230.0, 0.0, 0.0, 42.0, 6.0),
        f("smoked_fish", "Smoked fish in sauce", listOf("Ekyennyanja ekikalu"), MEAT_FISH, "1 piece with sauce", 200, 280.0, 6.0, 1.5, 34.0, 13.0),
        f("eggs_boiled", "Boiled eggs", listOf("Amagi"), MEAT_FISH, "2 eggs", 100, 155.0, 1.1, 0.0, 13.0, 11.0),
        f("eggs_fried", "Fried eggs / Spanish omelette", listOf("Amagi amasiike"), MEAT_FISH, "2 eggs", 120, 230.0, 3.0, 0.5, 14.0, 18.0),
        f("liver", "Liver, fried", listOf("Ekibumba"), MEAT_FISH, "1 portion", 150, 260.0, 6.0, 0.0, 34.0, 11.0),

        // ---- Vegetables & greens ----
        f("sukuma", "Sukuma wiki (collard greens)", listOf("Sukuma wiki"), VEGETABLE, "1 cup", 150, 90.0, 9.0, 4.0, 3.5, 5.0),
        f("dodo", "Dodo (amaranth greens)", listOf("Doodo", "Bbugga"), VEGETABLE, "1 cup", 150, 80.0, 7.0, 3.5, 4.0, 4.0),
        f("nakati", "Nakati (African eggplant leaves)", listOf("Nakati"), VEGETABLE, "1 cup", 150, 70.0, 6.0, 3.0, 4.0, 4.0),
        f("cabbage", "Stewed cabbage", listOf("Cabbage", "Kabeeji"), VEGETABLE, "1 cup", 200, 100.0, 12.0, 5.0, 3.0, 5.0),
        f("kachumbari", "Kachumbari (tomato & onion salad)", listOf("Kachumbari"), VEGETABLE, "1 side bowl", 150, 40.0, 9.0, 2.5, 1.5, 0.3),
        f("avocado", "Avocado", listOf("Ovakedo", "Avoko"), VEGETABLE, "½ large avocado", 100, 160.0, 8.5, 6.7, 2.0, 15.0),
        f("mushrooms", "Mushrooms, sautéed", listOf("Obutiko", "Butiko"), VEGETABLE, "1 cup", 150, 60.0, 5.0, 2.0, 5.0, 3.0),
        f("pumpkin", "Pumpkin, boiled", listOf("Ensujju", "Sujju"), VEGETABLE, "1 cup", 200, 52.0, 13.0, 2.4, 2.0, 0.2),

        // ---- Fruits ----
        f("sweet_banana", "Sweet banana", listOf("Ndizi", "Menvu", "Bogoya"), FRUIT, "1 medium", 100, 89.0, 23.0, 2.6, 1.1, 0.3),
        f("mango", "Mango", listOf("Muyembe", "Emiyembe"), FRUIT, "1 medium", 200, 120.0, 30.0, 3.2, 1.6, 0.8),
        f("pineapple", "Pineapple", listOf("Nanaasi"), FRUIT, "1 cup chunks", 165, 82.0, 22.0, 2.3, 0.9, 0.2),
        f("jackfruit", "Jackfruit", listOf("Fene", "Ffene"), FRUIT, "1 cup", 165, 157.0, 38.0, 2.5, 2.8, 1.0),
        f("passion", "Passion fruit", listOf("Butunda", "Akatunda"), FRUIT, "2 fruits", 36, 35.0, 8.4, 3.8, 0.8, 0.3),
        f("watermelon", "Watermelon", listOf("Watermelon", "Tikiti"), FRUIT, "1 slice", 280, 85.0, 21.0, 1.1, 1.7, 0.4),
        f("papaya", "Pawpaw (papaya)", listOf("Papaali"), FRUIT, "1 cup", 150, 65.0, 16.0, 2.5, 0.7, 0.4),
        f("sugarcane", "Sugarcane", listOf("Ebikajjo", "Kikajjo"), FRUIT, "1 stick chewed", 100, 60.0, 15.0, 0.0, 0.0, 0.0),

        // ---- Drinks ----
        f("african_tea", "African tea (milk + sugar)", listOf("Chai", "Caayi"), DRINK, "1 cup", 250, 120.0, 18.0, 0.0, 3.5, 4.0),
        f("black_coffee", "Black coffee / tea, no sugar", listOf("Kaawa", "Chai kavu"), DRINK, "1 cup", 250, 5.0, 0.0, 0.0, 0.3, 0.0),
        f("soda", "Soda (cola / orange / soda water with sugar)", listOf("Coca-Cola", "Pepsi", "Riham Cola", "Mirinda", "Fanta", "Novida"), DRINK, "500 ml bottle", 500, 210.0, 53.0, 0.0, 0.0, 0.0),
        f("soda_diet", "Diet / zero sugar soda", listOf("Coke Zero", "Diet soda"), DRINK, "500 ml bottle", 500, 2.0, 0.0, 0.0, 0.0, 0.0),
        f("water", "Bottled water", listOf("Rwenzori", "Aqua Sipi", "Highland"), DRINK, "500 ml", 500, 0.0, 0.0, 0.0, 0.0, 0.0),
        f("juice_boxed", "Boxed / bottled fruit juice", listOf("Minute Maid", "Splash", "Riham juice", "Cheers"), DRINK, "500 ml", 500, 230.0, 56.0, 0.5, 0.5, 0.0),
        f("fresh_juice", "Fresh passion / mango juice (sweetened)", listOf("Juice"), DRINK, "1 glass", 300, 150.0, 37.0, 1.0, 1.0, 0.3),
        f("milk", "Whole milk (packet)", listOf("Fresh Dairy", "Jesa", "Lato", "Amata"), DRINK, "500 ml packet", 500, 320.0, 24.0, 0.0, 16.0, 18.0),
        f("yoghurt_sweet", "Sweetened yoghurt", listOf("Fresh Dairy yoghurt", "Jesa yoghurt", "Bio yoghurt"), DRINK, "500 ml", 500, 450.0, 70.0, 0.0, 16.0, 12.0),
        f("beer", "Lager beer", listOf("Nile Special", "Bell", "Club", "Tusker"), DRINK, "500 ml bottle", 500, 215.0, 17.0, 0.0, 2.0, 0.0),
        f("waragi", "Spirit (gin / waragi)", listOf("Uganda Waragi", "Kasese"), DRINK, "1 shot", 50, 110.0, 0.0, 0.0, 0.0, 0.0),
        f("bushera", "Bushera (sorghum/millet drink)", listOf("Bushera", "Obushera"), DRINK, "1 cup", 300, 150.0, 33.0, 2.0, 3.0, 1.0),
        f("millet_porridge", "Millet porridge with sugar", listOf("Obusera", "Bushera bwa kalo"), DRINK, "1 mug", 300, 200.0, 42.0, 2.0, 4.0, 1.5),

        // ---- Supermarket & shop items ----
        f("bread", "White sliced bread", listOf("Hot Loaf", "Tip Top", "Supermarket bread", "Mugaati"), PACKAGED, "2 slices", 60, 160.0, 30.0, 1.5, 5.0, 2.0),
        f("sugar", "Sugar", listOf("Kakira", "Kinyara", "Sukaali"), PACKAGED, "2 teaspoons", 10, 40.0, 10.0, 0.0, 0.0, 0.0),
        f("margarine", "Margarine / butter", listOf("Blue Band", "Butter"), PACKAGED, "1 tablespoon", 14, 100.0, 0.0, 0.0, 0.0, 11.0),
        f("cooking_oil", "Cooking oil", listOf("Mukwano", "Golden Fry", "Fortune"), PACKAGED, "1 tablespoon", 14, 124.0, 0.0, 0.0, 0.0, 14.0),
        f("biscuits", "Sweet biscuits", listOf("Nice", "Glucose biscuits", "Marie", "Britania"), PACKAGED, "4 biscuits", 30, 140.0, 21.0, 0.5, 2.0, 5.5),
        f("crisps", "Potato crisps", listOf("Crisps", "Pringles"), PACKAGED, "1 packet", 50, 270.0, 26.0, 2.0, 3.0, 17.0),
        f("peanut_butter", "Peanut butter", listOf("Peanut butter", "Ebinyebwa ebisekule"), PACKAGED, "2 tablespoons", 32, 190.0, 7.0, 2.0, 8.0, 16.0),
        f("cheese", "Cheese (cheddar / gouda)", listOf("Cheese"), PACKAGED, "2 slices", 30, 120.0, 0.4, 0.0, 7.0, 10.0),
        f("sausages", "Beef / pork sausages", listOf("Sausages", "Fresh Cuts", "Farmer's Choice"), PACKAGED, "2 sausages", 100, 280.0, 6.0, 0.0, 12.0, 23.0),
        f("weetabix", "Wheat biscuits cereal", listOf("Weetabix"), PACKAGED, "2 biscuits", 37, 135.0, 25.0, 3.8, 4.4, 0.8),
        f("cornflakes", "Cornflakes", listOf("Kellogg's", "Cornflakes"), PACKAGED, "1 bowl (no milk)", 30, 113.0, 25.0, 1.0, 2.0, 0.3),
        f("ice_cream", "Ice cream", listOf("Ice cream"), PACKAGED, "2 scoops", 100, 207.0, 24.0, 0.7, 3.5, 11.0),
        f("chocolate", "Milk chocolate bar", listOf("Cadbury", "Chocolate"), PACKAGED, "1 bar", 45, 240.0, 26.0, 0.9, 3.4, 13.0),
        f("sardines", "Tinned sardines", listOf("Sardines", "Tinned fish"), PACKAGED, "1 tin", 125, 260.0, 0.0, 0.0, 25.0, 18.0),
        f("roast_chicken_sm", "Supermarket rotisserie chicken", listOf("Roast chicken"), PACKAGED, "1 quarter", 200, 440.0, 0.0, 0.0, 50.0, 26.0),
        f("coconut", "Fresh coconut", listOf("Coconut", "Nazi"), PACKAGED, "1 piece", 50, 177.0, 7.6, 4.5, 1.7, 17.0),
        f("ghee", "Ghee", listOf("Ghee", "Muzigo"), PACKAGED, "1 tablespoon", 14, 123.0, 0.0, 0.0, 0.0, 14.0),
        f("yoghurt_plain", "Plain unsweetened yoghurt", listOf("Natural yoghurt"), PACKAGED, "1 cup", 245, 150.0, 11.0, 0.0, 8.5, 8.0),
    )

    fun byId(id: String): FoodItem? = foods.firstOrNull { it.id == id }

    fun search(query: String): List<FoodItem> = foods.filter { it.matches(query) }

    /** Compact reference table for the AI recogniser prompt. */
    fun asPromptReference(): String = buildString {
        appendLine("name | local names | serving | kcal | carbs g | fibre g | protein g | fat g")
        foods.forEach { f ->
            val n = f.nutrition
            appendLine("${f.name} | ${f.localNames.joinToString("/")} | ${f.serving} (${f.servingGrams} g) | ${n.calories.r0()} | ${n.carbs.r1()} | ${n.fiber.r1()} | ${n.protein.r1()} | ${n.fat.r1()}")
        }
    }
}
