package app.dinar.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import app.dinar.core.model.AccountType
import app.dinar.core.model.Category
import app.dinar.core.model.CategoryIcon

val CategoryIcon.imageVector: ImageVector
    get() = when (this) {
        CategoryIcon.GROCERIES -> Icons.Rounded.ShoppingCart
        CategoryIcon.RESTAURANT -> Icons.Rounded.Restaurant
        CategoryIcon.TRANSPORT -> Icons.Rounded.DirectionsBus
        CategoryIcon.CAR -> Icons.Rounded.DirectionsCar
        CategoryIcon.HOME -> Icons.Rounded.Home
        CategoryIcon.BILLS -> Icons.Rounded.Bolt
        CategoryIcon.INTERNET -> Icons.Rounded.Wifi
        CategoryIcon.PHONE -> Icons.Rounded.PhoneAndroid
        CategoryIcon.SHOPPING -> Icons.Rounded.ShoppingBag
        CategoryIcon.CLOTHES -> Icons.Rounded.Checkroom
        CategoryIcon.HEALTH -> Icons.Rounded.LocalHospital
        CategoryIcon.EDUCATION -> Icons.Rounded.School
        CategoryIcon.ENTERTAINMENT -> Icons.Rounded.SportsEsports
        CategoryIcon.TRAVEL -> Icons.Rounded.Flight
        CategoryIcon.GIFT -> Icons.Rounded.CardGiftcard
        CategoryIcon.PETS -> Icons.Rounded.Pets
        CategoryIcon.SPORTS -> Icons.Rounded.FitnessCenter
        CategoryIcon.BEAUTY -> Icons.Rounded.Spa
        CategoryIcon.KIDS -> Icons.Rounded.ChildCare
        CategoryIcon.CHARITY -> Icons.Rounded.VolunteerActivism
        CategoryIcon.SALARY -> Icons.Rounded.Work
        CategoryIcon.FREELANCE -> Icons.Rounded.LaptopMac
        CategoryIcon.INVESTMENT -> Icons.Rounded.Savings
        CategoryIcon.RENT_INCOME -> Icons.Rounded.Apartment
        CategoryIcon.REFUND -> Icons.Rounded.Replay
        CategoryIcon.SALE -> Icons.Rounded.Sell
        CategoryIcon.BONUS -> Icons.Rounded.EmojiEvents
        CategoryIcon.OTHER -> Icons.Rounded.Category
    }

val AccountType.imageVector: ImageVector
    get() = when (this) {
        AccountType.CASH -> Icons.Rounded.Payments
        AccountType.BANK_CARD -> Icons.Rounded.CreditCard
        AccountType.SAVINGS -> Icons.Rounded.AccountBalance
        AccountType.WALLET -> Icons.Rounded.Wallet
    }

val AccountType.label: String
    get() = when (this) {
        AccountType.CASH -> "نقدی"
        AccountType.BANK_CARD -> "کارت بانکی"
        AccountType.SAVINGS -> "پس‌انداز"
        AccountType.WALLET -> "کیف پول"
    }

val Category.color: Color get() = Color(colorArgb)
