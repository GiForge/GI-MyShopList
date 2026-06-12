package com.example.ui.parser

import com.example.data.model.ShoppingItem
import java.util.Locale

data class CatalogEntry(val emoji: String, val category: String, val price: Double)

object ItalianVocalParser {

    private val numberWords = mapOf(
        "un" to 1, "uno" to 1, "una" to 1, "un'" to 1,
        "due" to 2, "tre" to 3, "quattro" to 4, "cinque" to 5,
        "sei" to 6, "sette" to 7, "otto" to 8, "nove" to 9, "dieci" to 10
    )

    private val unitKeywords = listOf(
        "kg", "g", "l", "ml", "litri", "litro", "chilo", "chili", "grammi",
        "pacco", "pacchi", "scatola", "scatole", "bottiglia", "bottiglie",
        "barattolo", "barattoli", "pezzo", "pezzi", "pz", "confezione", "confezioni",
        "conf", "scatole", "tubo", "tubetti"
    )

    val catalog = mapOf(
        // BEVANDE E BRAND ICONICI
        "coca cola" to CatalogEntry("🥤", "Bevande", 1.99),
        "fanta" to CatalogEntry("🍊", "Bevande", 1.79),
        "sprite" to CatalogEntry("🍋", "Bevande", 1.79),
        "pepsi" to CatalogEntry("🥤", "Bevande", 1.79),
        "estathé" to CatalogEntry("🍹", "Bevande", 1.39),
        "birra" to CatalogEntry("🍺", "Bevande", 1.49),
        "vino" to CatalogEntry("🍷", "Bevande", 5.99),
        "succo di frutta" to CatalogEntry("🧃", "Bevande", 1.89),
        "santanna" to CatalogEntry("💧", "Bevande", 0.49),
        "levissima" to CatalogEntry("🏔️", "Bevande", 0.59),
        "san pellegrino" to CatalogEntry("🫧", "Bevande", 0.89),
        "chinotto" to CatalogEntry("🟤", "Bevande", 1.29),

        "lasagna" to CatalogEntry("🥘", "Gastronomia", 6.90),
        "lasagne" to CatalogEntry("🥘", "Gastronomia", 6.90),
        "pollo arrosto" to CatalogEntry("🍗", "Gastronomia", 7.90),
        "patate arrosto" to CatalogEntry("🥔", "Gastronomia", 3.50),
        "pomodori ripieni" to CatalogEntry("🍅", "Gastronomia", 4.90),
        "riso freddo" to CatalogEntry("🍚", "Gastronomia", 3.90),
        "insalata di riso" to CatalogEntry("🍚", "Gastronomia", 3.90),
        "arrosto" to CatalogEntry("🍖", "Gastronomia", 9.90),
        "vitello tonnato" to CatalogEntry("🍱", "Gastronomia", 8.90),
        "parmigiana" to CatalogEntry("🍆", "Gastronomia", 5.90),
        "insalata russa" to CatalogEntry("🥣", "Gastronomia", 4.50),
        "oliva ascolana" to CatalogEntry("🫒", "Gastronomia", 5.50),
        "supplì" to CatalogEntry("🍘", "Gastronomia", 4.00),
        "arancino" to CatalogEntry("🍊", "Gastronomia", 3.50),
        "cous cous" to CatalogEntry("🥣", "Gastronomia", 4.50),
        "baccalà" to CatalogEntry("🐟", "Gastronomia", 8.90),
        "pizza" to CatalogEntry("🍕", "Gastronomia", 4.90),
        "focaccia" to CatalogEntry("🍞", "Gastronomia", 2.50),
        "pizzette" to CatalogEntry("🍕", "Gastronomia", 3.50),
        "calzone" to CatalogEntry("🥟", "Gastronomia", 3.90),
        "panino" to CatalogEntry("🍔", "Gastronomia", 2.90),
        "piadina" to CatalogEntry("🫓", "Gastronomia", 3.50),
        "rosticceria" to CatalogEntry("🥐", "Gastronomia", 5.00),
        "pane" to CatalogEntry("🥖", "Gastronomia", 1.50),
        "pane confezionato" to CatalogEntry("🍞", "Gastronomia", 1.80),
        "pane in cassetta" to CatalogEntry("🥪", "Gastronomia", 1.20),
        
        // PESCHERIA E PRODOTTI ITTICI / SURGELATI
        "pesce" to CatalogEntry("🐟", "Pescheria", 7.90),
        "salmone" to CatalogEntry("🍣", "Pescheria", 9.90),
        "tonno" to CatalogEntry("🐟", "Pescheria", 7.50),
        "merluzzo" to CatalogEntry("🐟", "Pescheria", 6.90),
        "orata" to CatalogEntry("🐟", "Pescheria", 8.90),
        "branzino" to CatalogEntry("🐟", "Pescheria", 9.50),
        "gamberi" to CatalogEntry("🍤", "Pescheria", 10.90),
        "gamberetti" to CatalogEntry("🍤", "Pescheria", 7.90),
        "calamari" to CatalogEntry("🦑", "Pescheria", 8.50),
        "polpo" to CatalogEntry("🐙", "Pescheria", 9.90),
        "cozze" to CatalogEntry("🦪", "Pescheria", 4.90),
        "vongole" to CatalogEntry("🦪", "Pescheria", 5.90),
        "bastoncini" to CatalogEntry("🐟", "Surgelati", 3.50),
        "fritto misto" to CatalogEntry("🍤", "Gastronomia", 7.90),
        
        // VERDURE E CONTORNI SURGELATI
        "minestrone" to CatalogEntry("🍲", "Surgelati", 2.19),
        "piselli surgelati" to CatalogEntry("🫛", "Surgelati", 1.89),
        "spinaci surgelati" to CatalogEntry("🥬", "Surgelati", 1.69),
        "patatine fritte" to CatalogEntry("🍟", "Surgelati", 2.29),
        "contorno leggero" to CatalogEntry("🥗", "Surgelati", 2.49),
        
        // PIATTI PRONTI E ROSTICCERIA SURGELATA
        "pizza surgelata" to CatalogEntry("🍕", "Surgelati", 3.29),
        "sofficini" to CatalogEntry("🥟", "Surgelati", 2.79),
        "lasagna surgelata" to CatalogEntry("🍲", "Surgelati", 3.99),
        "olive ascolane" to CatalogEntry("🫒", "Surgelati", 2.89),
        "crocchette" to CatalogEntry("🥔", "Surgelati", 1.99),
        "arancini" to CatalogEntry("🍘", "Surgelati", 2.99),
        
        // GELATI
        "gelato" to CatalogEntry("🍨", "Surgelati", 3.49),
        "cornetti" to CatalogEntry("🍦", "Surgelati", 3.29),
        "vaschetta gelato" to CatalogEntry("🍧", "Surgelati", 3.99),
        "torta surgelata" to CatalogEntry("🍰", "Surgelati", 4.99),
        "cornetti colazione" to CatalogEntry("🥐", "Surgelati", 2.79),

        // MACELLERIA E CARNE
        "carne" to CatalogEntry("🥩", "Macelleria", 9.90),
        "manzo" to CatalogEntry("🥩", "Macelleria", 12.90),
        "vitello" to CatalogEntry("🥩", "Macelleria", 14.90),
        "maiale" to CatalogEntry("🐖", "Macelleria", 8.90),
        "pollo" to CatalogEntry("🍗", "Macelleria", 5.90),
        "tacchino" to CatalogEntry("🦃", "Macelleria", 7.90),
        "hamburger" to CatalogEntry("🍔", "Macelleria", 4.90),
        "macinato" to CatalogEntry("🥩", "Macelleria", 7.90),
        "fettine" to CatalogEntry("🥩", "Macelleria", 9.90),
        "petto di pollo" to CatalogEntry("🍗", "Macelleria", 6.90),
        "cosce di pollo" to CatalogEntry("🍗", "Macelleria", 5.50),
        "salsiccia" to CatalogEntry("🌭", "Macelleria", 4.90),
        "costine" to CatalogEntry("🦴", "Macelleria", 6.90),
        "bistecca" to CatalogEntry("🥩", "Macelleria", 13.90),
        "fiorentina" to CatalogEntry("🥩", "Macelleria", 22.00),
        "cotoletta" to CatalogEntry("🍞", "Macelleria", 8.90),
        "nuggets" to CatalogEntry("🍗", "Macelleria", 3.90),
        "arista" to CatalogEntry("🥩", "Macelleria", 8.50),
        "spezzatino" to CatalogEntry("🍲", "Macelleria", 9.90),
        "spiedini" to CatalogEntry("🍢", "Macelleria", 5.90),
        "coniglio" to CatalogEntry("🐇", "Macelleria", 7.90),
        "fegato" to CatalogEntry("🥩", "Macelleria", 5.90),
        
        // INSACCATI E SALUMI
        "crudo" to CatalogEntry("🍖", "Salumi", 3.49),
        "cotto" to CatalogEntry("🐷", "Salumi", 2.29),
        "prosciutto crudo" to CatalogEntry("🍖", "Salumi", 3.49),
        "prosciutto cotto" to CatalogEntry("🐷", "Salumi", 2.29),
        "salame" to CatalogEntry("🥖", "Salumi", 2.49),
        "mortadella" to CatalogEntry("🍥", "Salumi", 1.99),
        "bresaola" to CatalogEntry("🥩", "Salumi", 4.49),
        "pancetta" to CatalogEntry("🥓", "Salumi", 1.89),
        "bacon" to CatalogEntry("🥓", "Salumi", 1.99),
        "speck" to CatalogEntry("🍖", "Salumi", 2.69),
        "coppa" to CatalogEntry("🥖", "Salumi", 2.59),
        "capocollo" to CatalogEntry("🥖", "Salumi", 2.79),
        "guanciale" to CatalogEntry("🥓", "Salumi", 2.19),
        "wurstel" to CatalogEntry("🌭", "Salumi", 1.29),
        "porchetta" to CatalogEntry("🐖", "Salumi", 2.89),
        "fusi di pollo" to CatalogEntry("🍗", "Salumi", 2.49),
        
        // SNACK DOLCI E MERENDINE
        "nutella" to CatalogEntry("🍫", "Dolci", 3.49),
        "crema novi" to CatalogEntry("⭐", "Dolci", 3.29),
        "crema pan di stelle" to CatalogEntry("⭐", "Dolci", 3.29),
        "merendine" to CatalogEntry("🥐", "Dolci", 2.49),
        "kinder brioss" to CatalogEntry("🍰", "Dolci", 2.99),
        "kinder delice" to CatalogEntry("🍩", "Dolci", 2.89),
        "bueno" to CatalogEntry("🍫", "Dolci", 1.49),
        "fiesta" to CatalogEntry("🍰", "Dolci", 2.49),
        "nastrine" to CatalogEntry("🥐", "Dolci", 2.29),
        "plumcake" to CatalogEntry("🍞", "Dolci", 1.89),
        "crostatine" to CatalogEntry("🥧", "Dolci", 2.49),
        "muffin" to CatalogEntry("🧁", "Dolci", 1.99),
        "cioccolato" to CatalogEntry("🍫", "Dolci", 2.49),
        "tavoletta" to CatalogEntry("🍫", "Dolci", 2.49),
        "zucchero" to CatalogEntry("🍬", "Dolci", 1.29),
        "fondente" to CatalogEntry("🍫", "Dolci", 2.69),
        "lindt" to CatalogEntry("🍬", "Dolci", 3.99),
        "ferrero rocher" to CatalogEntry("🟡", "Dolci", 4.99),
        "caramelle" to CatalogEntry("🍬", "Dolci", 1.49),
        "gomme" to CatalogEntry("🦷", "Dolci", 0.99),

        // COLAZIONE E CAFFÈ
        "caffè" to CatalogEntry("☕", "Colazione", 3.29),
        "ginseng" to CatalogEntry("☕", "Colazione", 3.49),
        "capsule" to CatalogEntry("☕", "Colazione", 4.99),
        "cialde" to CatalogEntry("☕", "Colazione", 4.49),
        "nespresso" to CatalogEntry("☕", "Colazione", 5.99),
        "lavazza" to CatalogEntry("☕", "Colazione", 3.99),
        "biscotti" to CatalogEntry("🍪", "Colazione", 1.99),
        "waffer" to CatalogEntry("🍪", "Colazione", 1.49),
        "ringo" to CatalogEntry("🍪", "Colazione", 1.79),
        "oreo" to CatalogEntry("🍪", "Colazione", 2.29),
        "macine" to CatalogEntry("🍪", "Colazione", 1.89),
        "gocciole" to CatalogEntry("🍪", "Colazione", 1.89),
        "pan di stelle" to CatalogEntry("⭐", "Colazione", 2.19),
        "tarallucci" to CatalogEntry("🍪", "Colazione", 1.69),
        "abbracci" to CatalogEntry("🍪", "Colazione", 1.79),
        "galletti" to CatalogEntry("🍪", "Colazione", 1.59),
        "oro saiwa" to CatalogEntry("🍪", "Colazione", 1.79),
        "cereali" to CatalogEntry("🥣", "Colazione", 2.99),
        "corn flakes" to CatalogEntry("🥣", "Colazione", 2.79),
        "muesli" to CatalogEntry("🥣", "Colazione", 3.29),
        "nesquik" to CatalogEntry("🍫", "Colazione", 3.49),
        "fette biscottate" to CatalogEntry("🍞", "Colazione", 1.49),
        "marmellata" to CatalogEntry("🍯", "Colazione", 2.49),

        // SNACK SALATI E APERITIVO
        "patatine" to CatalogEntry("🍟", "Snack", 1.89),
        "fonzies" to CatalogEntry("🧀", "Snack", 1.49),
        "pringles" to CatalogEntry("🥫", "Snack", 2.79),
        "san carlo" to CatalogEntry("🍟", "Snack", 1.89),
        "popcorn" to CatalogEntry("🍿", "Snack", 1.29),
        "salatini" to CatalogEntry("🥨", "Snack", 1.49),
        "cracker" to CatalogEntry("🧇", "Snack", 1.39),
        "taralli" to CatalogEntry("🥨", "Snack", 1.99),
        "grissini" to CatalogEntry("🥖", "Snack", 1.29),
        "noccioline" to CatalogEntry("🥜", "Snack", 1.79),
        "pistacchi" to CatalogEntry("🟢", "Snack", 3.99),

        // DISPENSA E SCATOLAME
        "sale" to CatalogEntry("🧂", "Dispensa", 0.49),
        "sale grosso" to CatalogEntry("🧂", "Dispensa", 0.49),
        "pasta" to CatalogEntry("🍝", "Dispensa", 1.29),
        "barilla" to CatalogEntry("🍝", "Dispensa", 1.39),
        "de cecco" to CatalogEntry("🍝", "Dispensa", 1.69),
        "rummo" to CatalogEntry("🍝", "Dispensa", 1.79),
        "riso" to CatalogEntry("🍚", "Dispensa", 1.49),
        "scotti" to CatalogEntry("🍚", "Dispensa", 1.59),
        "rio mare" to CatalogEntry("🥫", "Dispensa", 2.49),
        "simmenthal" to CatalogEntry("🥫", "Dispensa", 2.99),
        "passata" to CatalogEntry("🥫", "Dispensa", 0.99),
        "mutti" to CatalogEntry("🥫", "Dispensa", 1.39),
        "pesto" to CatalogEntry("🌿", "Dispensa", 2.29),
        "rana" to CatalogEntry("🥟", "Fresco", 3.49),
        "tortellini" to CatalogEntry("🥟", "Fresco", 2.99),
        "gnocchi" to CatalogEntry("🥔", "Fresco", 1.59),
        "maionese" to CatalogEntry("🧴", "Dispensa", 1.79),
        "calvé" to CatalogEntry("🧴", "Dispensa", 1.89),
        "ketchup" to CatalogEntry("🥫", "Dispensa", 1.49),
        "salse" to CatalogEntry("🥫", "Dispensa", 1.49),
        "legumi" to CatalogEntry("🫘", "Dispensa", 0.89),
        "ceci" to CatalogEntry("🫘", "Dispensa", 0.89),
        "lenticchie" to CatalogEntry("🫘", "Dispensa", 0.99),
        "fagioli" to CatalogEntry("🫘", "Dispensa", 0.89),

        // FRESCHI E LATTICINI
        "latte" to CatalogEntry("🥛", "Latticini", 1.39),
        "parmalat" to CatalogEntry("🥛", "Latticini", 1.49),
        "granarolo" to CatalogEntry("🥛", "Latticini", 1.49),
        "yogurt" to CatalogEntry("🍦", "Latticini", 0.89),
        "muller" to CatalogEntry("🍦", "Latticini", 0.99),
        "danone" to CatalogEntry("🍦", "Latticini", 0.99),
        "philadelphia" to CatalogEntry("🥣", "Latticini", 2.29),
        "galbani" to CatalogEntry("🧀", "Latticini", 2.99),
        "mozzarella" to CatalogEntry("⚪", "Latticini", 1.49),
        "ricotta" to CatalogEntry("🫕", "Latticini", 1.19),
        "uova" to CatalogEntry("🥚", "Latticini", 2.49),
        "burro" to CatalogEntry("🧈", "Latticini", 1.89),
        "certosa" to CatalogEntry("🧀", "Latticini", 1.79),
        "parmigiano" to CatalogEntry("🧀", "Latticini", 4.49),
        "reggiano" to CatalogEntry("🧀", "Latticini", 4.49),
        "grana padano" to CatalogEntry("🧀", "Latticini", 3.99),
        "pecorino" to CatalogEntry("🧀", "Latticini", 3.79),
        "provolone" to CatalogEntry("🧀", "Latticini", 2.89),
        "caciocavallo" to CatalogEntry("🧀", "Latticini", 3.29),
        "asiago" to CatalogEntry("🧀", "Latticini", 2.49),
        "fontina" to CatalogEntry("🧀", "Latticini", 2.99),
        "formaggio" to CatalogEntry("🧀", "Latticini", 2.99),
        "formaggio fresco" to CatalogEntry("🧀", "Latticini", 2.99),
        "gorgonzola" to CatalogEntry("🧀", "Latticini", 2.69),
        "taleggio" to CatalogEntry("🧀", "Latticini", 2.59),
        "scamorza" to CatalogEntry("🧀", "Latticini", 2.29),
        "emmental" to CatalogEntry("🧀", "Latticini", 2.19),
        "stracchino" to CatalogEntry("🥣", "Latticini", 1.69),
        "crescenza" to CatalogEntry("🥣", "Latticini", 1.69),
        "robbiola" to CatalogEntry("🥣", "Latticini", 1.59),
        "burrata" to CatalogEntry("⚪", "Latticini", 2.49),
        "mascarpone" to CatalogEntry("🥣", "Latticini", 2.39),
        "fiocchi di latte" to CatalogEntry("🥣", "Latticini", 1.29),
        "formaggio fuso" to CatalogEntry("🧀", "Latticini", 1.49),

        // ORTOFRUTTA
        "frutta" to CatalogEntry("🍎", "Ortofrutta", 2.99),
        "fragole" to CatalogEntry("🍓", "Ortofrutta", 2.99),
        "mele" to CatalogEntry("🍎", "Ortofrutta", 2.49),
        "pere" to CatalogEntry("🍐", "Ortofrutta", 2.29),
        "banana" to CatalogEntry("🍌", "Ortofrutta", 1.79),
        "arance" to CatalogEntry("🍊", "Ortofrutta", 1.99),
        "limoni" to CatalogEntry("🍋", "Ortofrutta", 1.49),
        "pesche" to CatalogEntry("🍑", "Ortofrutta", 2.49),
        "susine" to CatalogEntry("🍇", "Ortofrutta", 2.49),
        "uva" to CatalogEntry("🍇", "Ortofrutta", 2.99),
        "kiwi" to CatalogEntry("🥝", "Ortofrutta", 2.29),
        "melone" to CatalogEntry("🍈", "Ortofrutta", 2.49),
        "anguria" to CatalogEntry("🍉", "Ortofrutta", 3.49),
        "cocomero" to CatalogEntry("🍉", "Ortofrutta", 3.49),
        "frutta secca" to CatalogEntry("🥜", "Ortofrutta", 3.99),
        "pomodoro" to CatalogEntry("🍅", "Ortofrutta", 1.99),
        "pomodori" to CatalogEntry("🍅", "Ortofrutta", 1.99),
        "insalata" to CatalogEntry("🥬", "Ortofrutta", 1.29),
        "finocchi" to CatalogEntry("🥬", "Ortofrutta", 1.49),
        "patate" to CatalogEntry("🥔", "Ortofrutta", 1.49),
        "carote" to CatalogEntry("🥕", "Ortofrutta", 1.29),
        "zucchine" to CatalogEntry("🥒", "Ortofrutta", 1.79),
        "melanzane" to CatalogEntry("🍆", "Ortofrutta", 1.99),
        "peperoni" to CatalogEntry("🫑", "Ortofrutta", 2.49),
        "cipolla" to CatalogEntry("🧅", "Ortofrutta", 0.99),
        "aglio" to CatalogEntry("🧄", "Ortofrutta", 0.89),
        "peperoncino" to CatalogEntry("🌶️", "Ortofrutta", 0.79),
        "funghi" to CatalogEntry("🍄", "Ortofrutta", 2.49),
        "zucca" to CatalogEntry("🎃", "Ortofrutta", 1.99),
        "spinaci" to CatalogEntry("🥬", "Ortofrutta", 1.79),
        "asparagi" to CatalogEntry("🌱", "Ortofrutta", 3.49),
        "olive" to CatalogEntry("🫒", "Ortofrutta", 2.19),
        "broccoli" to CatalogEntry("🥦", "Ortofrutta", 1.99),
        "cavolfiore" to CatalogEntry("🥦", "Ortofrutta", 1.89),
        "carciofi" to CatalogEntry("🥬", "Ortofrutta", 2.99),
        "sedano" to CatalogEntry("🥬", "Ortofrutta", 1.19),
        "piselli" to CatalogEntry("🫛", "Ortofrutta", 1.99),
        "fagiolini" to CatalogEntry("🫛", "Ortofrutta", 2.29),
        "porri" to CatalogEntry("🥬", "Ortofrutta", 1.59),
        "ravanelli" to CatalogEntry("🥕", "Ortofrutta", 0.99),
        "rape" to CatalogEntry("🥕", "Ortofrutta", 1.29),

        // IGIENE E CASA
        "carta igienica" to CatalogEntry("🧻", "Casa", 3.99),
        "foxy" to CatalogEntry("🧻", "Casa", 3.99),
        "regina" to CatalogEntry("🧻", "Casa", 3.79),
        "scottex" to CatalogEntry("🧻", "Casa", 3.49),
        "carta forno" to CatalogEntry("📜", "Casa", 1.99),
        "alluminio" to CatalogEntry("🪙", "Casa", 1.99),
        "pellicola" to CatalogEntry("🌀", "Casa", 1.79),
        "sacchetti gelo" to CatalogEntry("❄️", "Casa", 1.49),
        "sacchi immondizia" to CatalogEntry("🗑️", "Casa", 2.29),
        "tovaglioli" to CatalogEntry("🧻", "Casa", 1.49),

        // PULIZIA E LAVANDERIA
        "detersivo lavatrice" to CatalogEntry("🧴", "Pulizia", 6.90),
        "dash" to CatalogEntry("🧼", "Pulizia", 6.49),
        "dixan" to CatalogEntry("🧼", "Pulizia", 5.99),
        "ammorbidente" to CatalogEntry("🌸", "Pulizia", 3.49),
        "candeggina" to CatalogEntry("🧪", "Pulizia", 1.29),
        "candeggina delicata" to CatalogEntry("🧪", "Pulizia", 1.79),
        "smacchiatore" to CatalogEntry("🧪", "Pulizia", 3.99),
        "detersivo piatti" to CatalogEntry("🧼", "Pulizia", 1.99),
        "detersivo lavastoviglie" to CatalogEntry("🍽️", "Pulizia", 4.99),
        "finish" to CatalogEntry("🍽️", "Pulizia", 5.49),
        "brillantante" to CatalogEntry("✨", "Pulizia", 3.49),
        "sale lavastoviglie" to CatalogEntry("🧂", "Pulizia", 1.49),
        "sgrassatore" to CatalogEntry("✨", "Pulizia", 2.99),
        "chantecair" to CatalogEntry("✨", "Pulizia", 3.49),
        "detersivo pavimenti" to CatalogEntry("🧹", "Pulizia", 2.49),
        "alcool" to CatalogEntry("🧴", "Pulizia", 1.49),
        "anticalcare" to CatalogEntry("✨", "Pulizia", 2.99),
        "wcrete" to CatalogEntry("🚽", "Pulizia", 1.99),
        "spugne" to CatalogEntry("🧽", "Pulizia", 1.49),
        "panni" to CatalogEntry("🧹", "Pulizia", 2.29),

        // IGIENE PERSONALE
        "fazzoletti" to CatalogEntry("🤧", "Igiene", 1.49),
        "shampoo" to CatalogEntry("🧴", "Igiene", 3.99),
        "bagnoschiuma" to CatalogEntry("🧼", "Igiene", 2.99),
        "balsamo" to CatalogEntry("🧴", "Igiene", 3.99),
        "sapone mani" to CatalogEntry("🧼", "Igiene", 1.99),
        "sapone liquido" to CatalogEntry("🧼", "Igiene", 2.29),
        "spazzolino" to CatalogEntry("🪥", "Igiene", 2.49),
        "dentifricio" to CatalogEntry("🪥", "Igiene", 2.99),
        "colgate" to CatalogEntry("🪥", "Igiene", 3.49),
        "colluttorio" to CatalogEntry("🧪", "Igiene", 3.49),
        "filo interdentale" to CatalogEntry("🧵", "Igiene", 2.49),
        "deodorante" to CatalogEntry("💨", "Igiene", 3.49),
        "dischetti cotone" to CatalogEntry("⚪", "Igiene", 1.49),
        "cotton fioc" to CatalogEntry("👂", "Igiene", 1.29),
        "assorbenti" to CatalogEntry("🩸", "Igiene", 2.99),
        "schiuma da barba" to CatalogEntry("🪒", "Igiene", 2.99),
        "rasoio" to CatalogEntry("🪒", "Igiene", 3.49),
        "lamette" to CatalogEntry("🪒", "Igiene", 4.99),
        "salviettine" to CatalogEntry("🧻", "Igiene", 1.99)
    )

    fun resolveFromCatalog(name: String): CatalogEntry {
        val lower = name.lowercase(Locale.ITALIAN).trim()
        
        // Try exact match first
        catalog[lower]?.let { return it }

        // Try fuzzy keyword containment check
        for ((key, entry) in catalog) {
            if (lower.contains(key) || key.contains(lower)) {
                return entry
            }
        }

        // Generic defaults
        return CatalogEntry("📦", "Altro", 1.50)
    }

    /**
     * Parses a spoken sentence or textual input in Italian.
     * E.g., "aggiungi 3 litri di latte intero e pane"
     */
    fun parseInput(input: String): List<ShoppingItem> {
        val normalized = input.lowercase(Locale.ITALIAN)
            .replace(",", " e ")
            .replace(";", " e ")
            .trim()

        val parts = normalized.split(" e ")
        val items = mutableListOf<ShoppingItem>()

        for (part in parts) {
            val trimmedPart = part.trim()
            if (trimmedPart.isEmpty()) continue

            // Clean leading command words
            val commands = listOf("aggiungi", "prendi", "comprami", "compra", "metti", "voglio", "inserisci", "comprare", "un", "una", "uno", "delle", "dei", "del")
            var cleaned = trimmedPart
            var changed = true
            while (changed) {
                changed = false
                for (cmd in commands) {
                    if (cleaned.startsWith("$cmd ")) {
                        cleaned = cleaned.substring(cmd.length).trim()
                        changed = true
                    }
                }
            }

            if (cleaned.isEmpty()) continue

            var quantity = 1
            var unit = ""
            var productName = cleaned

            // Try to extract numerical value at the beginning
            val words = cleaned.split("\\s+".toRegex())
            if (words.isNotEmpty()) {
                val firstWord = words[0]
                val qtyFromWord = numberWords[firstWord]
                val qtyFromDigits = firstWord.toIntOrNull()

                if (qtyFromDigits != null) {
                    quantity = qtyFromDigits
                    productName = words.drop(1).joinToString(" ")
                } else if (qtyFromWord != null) {
                    quantity = qtyFromWord
                    productName = words.drop(1).joinToString(" ")
                }

                // Check if the next word is a unit
                val newWords = productName.split("\\s+".toRegex())
                if (newWords.isNotEmpty()) {
                    val potentialUnit = newWords[0]
                    if (unitKeywords.contains(potentialUnit)) {
                        unit = potentialUnit
                        productName = newWords.drop(1).joinToString(" ")
                        
                        // Clean leading prepositions from remaining product name
                        val prepositions = listOf("di", "del", "della", "dei", "d'", "da")
                        val tempWords = productName.split("\\s+".toRegex())
                        if (tempWords.isNotEmpty() && prepositions.contains(tempWords[0])) {
                            productName = tempWords.drop(1).joinToString(" ")
                        }
                    }
                }
            }

            productName = productName.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ITALIAN) else it.toString() }
            if (productName.isNotEmpty()) {
                val catalogMatch = resolveFromCatalog(productName)
                items.add(
                    ShoppingItem(
                        name = productName,
                        category = catalogMatch.category,
                        quantity = quantity,
                        unit = unit,
                        price = catalogMatch.price,
                        emoji = catalogMatch.emoji
                    )
                )
            }
        }

        return items
    }
}
