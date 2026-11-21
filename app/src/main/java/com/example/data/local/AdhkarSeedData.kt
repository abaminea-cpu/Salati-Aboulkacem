package com.example.data.local

import com.example.data.local.db.DhikrEntity

data class AdhkarCategoryMeta(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconKey: String
)

object AdhkarSeedData {

    val categories = listOf(
        AdhkarCategoryMeta(
            id = "morning",
            title = "أذكار الصباح",
            subtitle = "تحصين اليوم وبركة الصباح",
            iconKey = "morning"
        ),
        AdhkarCategoryMeta(
            id = "evening",
            title = "أذكار المساء",
            subtitle = "السكينة والحفظ عند المساء",
            iconKey = "evening"
        ),
        AdhkarCategoryMeta(
            id = "after_prayer",
            title = "أذكار بعد الصلاة",
            subtitle = "الذكر والتسبيح عقب الصلوات المفروضة",
            iconKey = "prayer"
        ),
        AdhkarCategoryMeta(
            id = "sleep",
            title = "أذكار النوم",
            subtitle = "آيات وأدعية السكينة قبل النوم",
            iconKey = "sleep"
        ),
        AdhkarCategoryMeta(
            id = "wakeup",
            title = "أذكار الاستيقاظ",
            subtitle = "الحمد والشكر عند الاستيقاظ",
            iconKey = "wakeup"
        ),
        AdhkarCategoryMeta(
            id = "travel",
            title = "دعاء السفر والركوب",
            subtitle = "أدعية السفر والتنقل اليومي",
            iconKey = "travel"
        ),
        AdhkarCategoryMeta(
            id = "comprehensive",
            title = "أدعية قرآنية وجامعة",
            subtitle = "جوامع الدعاء والاستغفار",
            iconKey = "dua"
        )
    )

    val items: List<DhikrEntity> = listOf(
        // ================= أذكار الصباح =================
        DhikrEntity(
            id = "m_1",
            categoryId = "morning",
            title = "آية الكرسي",
            arabicText = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\nاللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            referenceText = "من قالها حين يصبح أُجير من الجن حتى يمسي — سورة البقرة: ٢٥٥",
            targetCount = 1,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            orderIndex = 1
        ),
        DhikrEntity(
            id = "m_2",
            categoryId = "morning",
            title = "سورة الإخلاص",
            arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ.",
            referenceText = "من قالها مع المعوذتين ثلاث مرات حين يصبح وحين يمسي كفته من كل شيء — رواه أبو داود والترمذي",
            targetCount = 3,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/112001.mp3",
            orderIndex = 2
        ),
        DhikrEntity(
            id = "m_3",
            categoryId = "morning",
            title = "سورة الفلق",
            arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ.",
            referenceText = "المعوذات ثلاث مرات صباحاً — حصن المسلم",
            targetCount = 3,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/113001.mp3",
            orderIndex = 3
        ),
        DhikrEntity(
            id = "m_4",
            categoryId = "morning",
            title = "سورة الناس",
            arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ.",
            referenceText = "المعوذات ثلاث مرات صباحاً — حصن المسلم",
            targetCount = 3,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/114001.mp3",
            orderIndex = 4
        ),
        DhikrEntity(
            id = "m_5",
            categoryId = "morning",
            title = "أصبحنا وأصبح الملك لله",
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَٰذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَٰذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ.",
            referenceText = "رواه مسلم",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 5
        ),
        DhikrEntity(
            id = "m_6",
            categoryId = "morning",
            title = "سيد الاستغفار",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
            referenceText = "من قالها موقناً بها فمات من يومه دخل الجنة — رواه البخاري",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 6
        ),
        DhikrEntity(
            id = "m_7",
            categoryId = "morning",
            title = "بسم الله الذي لا يضر مع اسمه شيء",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            referenceText = "من قالها ثلاثاً لم يضره شيء — رواه أبو داود والترمذي",
            targetCount = 3,
            audioUrl = null,
            orderIndex = 7
        ),
        DhikrEntity(
            id = "m_8",
            categoryId = "morning",
            title = "رضيت بالله رباً",
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا.",
            referenceText = "من قالها ثلاثاً كان حقاً على الله أن يرضيه — رواه أحمد والترمذي",
            targetCount = 3,
            audioUrl = null,
            orderIndex = 8
        ),
        DhikrEntity(
            id = "m_9",
            categoryId = "morning",
            title = "حسبي الله",
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ.",
            referenceText = "من قالها سبع مرات كفاه الله ما أهمه — رواه أبو داود",
            targetCount = 7,
            audioUrl = null,
            orderIndex = 9
        ),
        DhikrEntity(
            id = "m_10",
            categoryId = "morning",
            title = "سبحان الله وبحمده",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ.",
            referenceText = "من قالها مائة مرة حطت خطاياه وإن كانت مثل زبد البحر — متفق عليه",
            targetCount = 100,
            audioUrl = null,
            orderIndex = 10
        ),

        // ================= أذكار المساء =================
        DhikrEntity(
            id = "e_1",
            categoryId = "evening",
            title = "آية الكرسي",
            arabicText = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\nاللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            referenceText = "من قالها حين يمسي أُجير من الجن حتى يصبح — سورة البقرة: ٢٥٥",
            targetCount = 1,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            orderIndex = 1
        ),
        DhikrEntity(
            id = "e_2",
            categoryId = "evening",
            title = "المعوذات (الإخلاص والفلق والناس)",
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ.\nقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ...\nقُلْ أَعُوذُ بِرَبِّ النَّاسِ...",
            referenceText = "ثلاث مرات حين يمسي تكفيه من كل شيء — رواه الترمذي",
            targetCount = 3,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/112001.mp3",
            orderIndex = 2
        ),
        DhikrEntity(
            id = "e_3",
            categoryId = "evening",
            title = "أمسينا وأمسى الملك لله",
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَٰذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَٰذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ.",
            referenceText = "رواه مسلم",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 3
        ),
        DhikrEntity(
            id = "e_4",
            categoryId = "evening",
            title = "اللهم بك أمسينا",
            arabicText = "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ.",
            referenceText = "رواه الترمذي وابن ماجه",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 4
        ),
        DhikrEntity(
            id = "e_5",
            categoryId = "evening",
            title = "سيد الاستغفار",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
            referenceText = "من قالها من الليل موقناً بها فمات قبل أن يصبح دخل الجنة — رواه البخاري",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 5
        ),
        DhikrEntity(
            id = "e_6",
            categoryId = "evening",
            title = "أعوذ بكلمات الله التامات",
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
            referenceText = "من قالها ثلاث مرات حين يمسي لم يضره شيء في تلك الليلة — رواه مسلم",
            targetCount = 3,
            audioUrl = null,
            orderIndex = 6
        ),
        DhikrEntity(
            id = "e_7",
            categoryId = "evening",
            title = "بسم الله الذي لا يضر مع اسمه شيء",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            referenceText = "ثلاث مرات مساءً — رواه أبو داود والترمذي",
            targetCount = 3,
            audioUrl = null,
            orderIndex = 7
        ),
        DhikrEntity(
            id = "e_8",
            categoryId = "evening",
            title = "يا حي يا قيوم برحمتك أستغيث",
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ.",
            referenceText = "رواه النسائي والحاكم",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 8
        ),
        DhikrEntity(
            id = "e_9",
            categoryId = "evening",
            title = "الصلاة على النبي ﷺ",
            arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ وَبَارِكْ عَلَى نَبِيِّنَا مُحَمَّدٍ.",
            referenceText = "من صلى علي حين يصبح عشراً وحين يمسي عشراً أدركته شفاعتي — رواه الطبراني",
            targetCount = 10,
            audioUrl = null,
            orderIndex = 9
        ),

        // ================= أذكار بعد الصلاة =================
        DhikrEntity(
            id = "ap_1",
            categoryId = "after_prayer",
            title = "الاستغفار بعد السلام",
            arabicText = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ.\nاللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ.",
            referenceText = "رواه مسلم",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 1
        ),
        DhikrEntity(
            id = "ap_2",
            categoryId = "after_prayer",
            title = "لا إله إلا الله وحده لا شريك له",
            arabicText = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ.",
            referenceText = "متفق عليه",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 2
        ),
        DhikrEntity(
            id = "ap_3",
            categoryId = "after_prayer",
            title = "التسبيح",
            arabicText = "سُبْحَانَ اللَّهِ.",
            referenceText = "ثلاثاً وثلاثين دبر كل صلاة — رواه مسلم",
            targetCount = 33,
            audioUrl = null,
            orderIndex = 3
        ),
        DhikrEntity(
            id = "ap_4",
            categoryId = "after_prayer",
            title = "التحميد",
            arabicText = "الْحَمْدُ لِلَّهِ.",
            referenceText = "ثلاثاً وثلاثين دبر كل صلاة — رواه مسلم",
            targetCount = 33,
            audioUrl = null,
            orderIndex = 4
        ),
        DhikrEntity(
            id = "ap_5",
            categoryId = "after_prayer",
            title = "التكبير",
            arabicText = "اللَّهُ أَكْبَرُ.",
            referenceText = "ثلاثاً وثلاثين دبر كل صلاة — رواه مسلم",
            targetCount = 33,
            audioUrl = null,
            orderIndex = 5
        ),
        DhikrEntity(
            id = "ap_6",
            categoryId = "after_prayer",
            title = "تمام المائة",
            arabicText = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
            referenceText = "غُفرت خطاياه وإن كانت مثل زبد البحر — رواه مسلم",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 6
        ),
        DhikrEntity(
            id = "ap_7",
            categoryId = "after_prayer",
            title = "آية الكرسي بعد كل صلاة",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            referenceText = "من قرأها دبر كل صلاة لم يمنعه من دخول الجنة إلا أن يموت — رواه النسائي",
            targetCount = 1,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            orderIndex = 7
        ),

        // ================= أذكار النوم =================
        DhikrEntity(
            id = "s_1",
            categoryId = "sleep",
            title = "باسمك ربي وضعت جنبي",
            arabicText = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ.",
            referenceText = "متفق عليه",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 1
        ),
        DhikrEntity(
            id = "s_2",
            categoryId = "sleep",
            title = "باسمك اللهم أموت وأحيا",
            arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا.",
            referenceText = "رواه البخاري",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 2
        ),
        DhikrEntity(
            id = "s_3",
            categoryId = "sleep",
            title = "اللهم قني عذابك",
            arabicText = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ.",
            referenceText = "ثلاث مرات عند وضع الرأس للنوم — رواه أبو داود والترمذي",
            targetCount = 3,
            audioUrl = null,
            orderIndex = 3
        ),
        DhikrEntity(
            id = "s_4",
            categoryId = "sleep",
            title = "آية الكرسي قبل النوم",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            referenceText = "لا يزال عليك من الله حافظ ولا يقربك شيطان حتى تصبح — رواه البخاري",
            targetCount = 1,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            orderIndex = 4
        ),
        DhikrEntity(
            id = "s_5",
            categoryId = "sleep",
            title = "تسبيح فاطمة رضي الله عنها",
            arabicText = "سُبْحَانَ اللَّهِ (٣٣)، وَالْحَمْدُ لِلَّهِ (٣٣)، وَاللَّهُ أَكْبَرُ (٣٤).",
            referenceText = "أفضل من خادم — متفق عليه",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 5
        ),
        DhikrEntity(
            id = "s_6",
            categoryId = "sleep",
            title = "دعاء تسليم النفس إلى الله",
            arabicText = "اللَّهُمَّ أَسْلَمْتُ نَفْسِي إِلَيْكَ، وَفَوَّضْتُ أَمْرِي إِلَيْكَ، وَوَجَّهْتُ وَجْهِي إِلَيْكَ، وَأَلْجَأْتُ ظَهْرِي إِلَيْكَ، رَغْبَةً وَرَهْبَةً إِلَيْكَ، لَا مَلْجَأَ وَلَا مَنْجَا مِنْكَ إِلَّا إِلَيْكَ، آمَنْتُ بِكِتَابِكَ الَّذِي أَنْزَلْتَ وَبِنَبِيِّكَ الَّذِي أَرْسَلْتَ.",
            referenceText = "متفق عليه",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 6
        ),

        // ================= أذكار الاستيقاظ =================
        DhikrEntity(
            id = "w_1",
            categoryId = "wakeup",
            title = "الحمد لله الذي أحيانا",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ.",
            referenceText = "رواه البخاري",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 1
        ),
        DhikrEntity(
            id = "w_2",
            categoryId = "wakeup",
            title = "الحمد لله الذي عافاني في جسدي",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي عَافَانِي فِي جَسَدِي، وَرَدَّ عَلَيَّ رُوحِي، وَأَذِنَ لِي بِذِكْرِهِ.",
            referenceText = "رواه الترمذي",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 2
        ),
        DhikrEntity(
            id = "w_3",
            categoryId = "wakeup",
            title = "دعاء التعار من الليل",
            arabicText = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، الْحَمْدُ لِلَّهِ، وَسُبْحَانَ اللَّهِ، وَلَا إِلَٰهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ.",
            referenceText = "من قالها ثم دعا استُجيب له — رواه البخاري",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 3
        ),

        // ================= دعاء السفر والركوب =================
        DhikrEntity(
            id = "t_1",
            categoryId = "travel",
            title = "دعاء ركوب الدابة أو السيارة",
            arabicText = "بِسْمِ اللَّهِ، الْحَمْدُ لِلَّهِ، سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ ۝ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ.",
            referenceText = "سورة الزخرف: ١٣-١٤ — رواه أبو داود والترمذي",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 1
        ),
        DhikrEntity(
            id = "t_2",
            categoryId = "travel",
            title = "دعاء السفر كامل",
            arabicText = "اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ، اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَٰذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى، اللَّهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هَٰذَا وَاطْوِ عَنَّا بُعْدَهُ، اللَّهُمَّ أَنْتَ الصَّاحِبُ فِي السَّفَرِ، وَالْخَلِيفَةُ فِي الْأَهْلِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ وَعْثَاءِ السَّفَرِ، وَكَآبَةِ الْمَنْظَرِ، وَسُوءِ الْمُنْقَلَبِ فِي الْمَالِ وَالْأَهْلِ.",
            referenceText = "رواه مسلم",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 2
        ),
        DhikrEntity(
            id = "t_3",
            categoryId = "travel",
            title = "دعاء الخروج من المنزل",
            arabicText = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ.",
            referenceText = "يقال له: هُديت وكُفيت ووُقيت وتنحى عنه الشيطان — رواه أبو داود والترمذي",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 3
        ),
        DhikrEntity(
            id = "t_4",
            categoryId = "travel",
            title = "دعاء الرجوع من السفر",
            arabicText = "آيِبُونَ تَائِبُونَ عَابِدُونَ لِرَبِّنَا حَامِدُونَ.",
            referenceText = "رواه البخاري ومسلم",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 4
        ),

        // ================= أدعية قرآنية وجامعة =================
        DhikrEntity(
            id = "c_1",
            categoryId = "comprehensive",
            title = "ربنا آتنا في الدنيا حسنة",
            arabicText = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ.",
            referenceText = "أكثر دعاء النبي ﷺ — سورة البقرة: ٢٠١",
            targetCount = 1,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002201.mp3",
            orderIndex = 1
        ),
        DhikrEntity(
            id = "c_2",
            categoryId = "comprehensive",
            title = "دعاء تفريج الهم والكرب",
            arabicText = "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ.",
            referenceText = "دعوة ذي النون لم يدعُ بها مسلم إلا استجاب الله له — سورة الأنبياء: ٨٧",
            targetCount = 3,
            audioUrl = null,
            orderIndex = 2
        ),
        DhikrEntity(
            id = "c_3",
            categoryId = "comprehensive",
            title = "دعاء الثبات والهدى",
            arabicText = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ.",
            referenceText = "رواه الترمذي",
            targetCount = 3,
            audioUrl = null,
            orderIndex = 3
        ),
        DhikrEntity(
            id = "c_4",
            categoryId = "comprehensive",
            title = "جوامع الدعاء",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى.",
            referenceText = "رواه مسلم",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 4
        ),
        DhikrEntity(
            id = "c_5",
            categoryId = "comprehensive",
            title = "دعاء العافية",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي.",
            referenceText = "رواه أبو داود وابن ماجه",
            targetCount = 1,
            audioUrl = null,
            orderIndex = 5
        ),
        DhikrEntity(
            id = "c_6",
            categoryId = "comprehensive",
            title = "كنز من كنوز الجنة",
            arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ.",
            referenceText = "متفق عليه",
            targetCount = 10,
            audioUrl = null,
            orderIndex = 6
        )
    )
}
