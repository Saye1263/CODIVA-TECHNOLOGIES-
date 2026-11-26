package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.ui.graphics.vector.ImageVector

data class AnnouncementItem(
    val id: String,
    val priority: String,
    val title: String,
    val message: String
)

data class PillarItem(
    val title: String,
    val description: String
)

data class ServiceItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector
)

data class ProductItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String
)

data class ProjectItem(
    val id: String,
    val title: String,
    val status: String,
    val category: String,
    val summary: String
)

data class AcademyProgram(
    val id: String,
    val title: String,
    val programType: String,
    val level: String,
    val duration: String,
    val headline: String,
    val description: String
)

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val category: String,
    val publishedDate: String,
    val fullStory: String
)

data class CareerRole(
    val id: String,
    val title: String,
    val department: String,
    val location: String,
    val employmentType: String,
    val closingDate: String,
    val summary: String
)

data class TeamMember(
    val name: String,
    val role: String,
    val group: String,
    val department: String,
    val bio: String,
    val initials: String
)

data class DepartmentSummary(
    val name: String,
    val headName: String,
    val staffCount: Int,
    val activeProjects: Int,
    val focus: String
)

data class SeedWorkspaceItem(
    val docId: String,
    val moduleType: WorkspaceModuleType,
    val title: String,
    val subtitle: String,
    val detail: String,
    val category: String,
    val status: String,
    val amountCents: Long = 0L
)

data class HelpFaqItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String
)

data class SeedPublicationItem(
    val docId: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: PublicationCategory,
    val targetRole: String,
    val documentUrl: String,
    val documentLabel: String,
    val expiresAtLabel: String,
    val status: PublicationStatus
)

data class SeedReleaseItem(
    val docId: String,
    val platform: ReleasePlatform,
    val versionName: String,
    val buildNumber: Long,
    val releaseNotes: String,
    val downloadUrl: String,
    val status: ReleaseStatus
)

object CodivaCatalog {
    const val PHONE = "+250 794 756 871"
    const val EMAIL = "info@codiva.com"
    const val HEADQUARTERS = "Monrovia, Liberia"
    const val SLOGAN = "Code. Create. Connect."
    const val OFFICIAL_DOMAIN = "Codiva.com"
    const val FOUNDER_EMAIL = "ebenezeryeapeh99@gmail.com"
    const val FOUNDER_NAME = "Ebenezer Yeapeh Jr."
    const val FOUNDER_MANTRA = "Together for a Digital and Innovative Africa"
    const val PUBLIC_PORTAL_URL = "https://ais-pre-ssjxhvm5g2ud75qmux6y7y-15631185691.europe-west2.run.app"
    const val DEV_PORTAL_URL = "https://ais-dev-ssjxhvm5g2ud75qmux6y7y-15631185691.europe-west2.run.app"

    val announcements = listOf(
        AnnouncementItem(
            id = "ann_holiday",
            priority = "IMPORTANT",
            title = "Holiday office hours",
            message = "Our Monrovia office will close at 2 p.m. on 18 December and reopen on 5 January. Support tickets are still monitored over the break."
        ),
        AnnouncementItem(
            id = "ann_community",
            priority = "NORMAL",
            title = "Welcome to CODIVA!",
            message = "Together, we build stronger communities, empower members and create lasting impact."
        ),
        AnnouncementItem(
            id = "ann_services",
            priority = "NORMAL",
            title = "Welcome to CODIVA Technologies",
            message = "Code. Create. Connect. Explore our services and see how we can help your business build software that works."
        )
    )

    val pillars = listOf(
        PillarItem(
            title = "Build",
            description = "Software, websites and mobile apps made for how you work."
        ),
        PillarItem(
            title = "Design",
            description = "Interfaces planned around the people who use them."
        ),
        PillarItem(
            title = "Advise",
            description = "Technology consulting and automation that remove manual work."
        ),
        PillarItem(
            title = "Belong",
            description = "A community of people who build and learn together."
        )
    )

    val services = listOf(
        ServiceItem(
            id = "srv_custom_software",
            title = "Custom software development",
            subtitle = "Bespoke systems",
            description = "Bespoke software built around how your business really works.",
            icon = Icons.Rounded.Code
        ),
        ServiceItem(
            id = "srv_web_design",
            title = "Website design and development",
            subtitle = "Web engineering",
            description = "Fast, mobile-friendly websites that bring in enquiries.",
            icon = Icons.Rounded.Language
        ),
        ServiceItem(
            id = "srv_mobile_apps",
            title = "Mobile application development",
            subtitle = "Android & iPhone",
            description = "Native and cross-platform apps for Android and iPhone.",
            icon = Icons.Rounded.Devices
        ),
        ServiceItem(
            id = "srv_automation",
            title = "Business systems and automation",
            subtitle = "Workflow automation",
            description = "Replace spreadsheets and manual steps with reliable systems.",
            icon = Icons.Rounded.SettingsSuggest
        ),
        ServiceItem(
            id = "srv_uiux",
            title = "UI/UX design",
            subtitle = "Human-centered interfaces",
            description = "Interfaces people understand at first use.",
            icon = Icons.Rounded.DesignServices
        )
    )

    val products = listOf(
        ProductItem(
            id = "prod_field_tracker",
            name = "CODIVA Field Tracker",
            category = "Mobile app",
            description = "Track field teams and jobs on a phone, even offline."
        ),
        ProductItem(
            id = "prod_sites_builder",
            name = "CODIVA Sites Builder",
            category = "Web platform",
            description = "Launch a branded website without writing code."
        ),
        ProductItem(
            id = "prod_ledger",
            name = "CODIVA Ledger",
            category = "Business system",
            description = "Simple bookkeeping for small and mid-sized businesses."
        )
    )

    val projects = listOf(
        ProjectItem(
            id = "proj_clinic_booking",
            title = "Clinic appointment booking app",
            status = "In progress",
            category = "Mobile app",
            summary = "Patients book, reschedule and get reminders from their phone."
        ),
        ProjectItem(
            id = "proj_coop_farm",
            title = "Co-operative farm website",
            status = "In progress",
            category = "Website",
            summary = "A website that helps members sell their produce online."
        ),
        ProjectItem(
            id = "proj_inventory",
            title = "Inventory system for a food distributor",
            status = "Completed",
            category = "Software",
            summary = "Live stock counts across three warehouses."
        ),
        ProjectItem(
            id = "proj_academy_lms",
            title = "Learning platform for a coding academy",
            status = "Planned",
            category = "Software",
            summary = "Courses, assignments and progress tracking in one place."
        ),
        ProjectItem(
            id = "proj_market_ordering",
            title = "Mobile ordering app for market traders",
            status = "Completed",
            category = "UI/UX",
            summary = "A simpler ordering flow tested with traders."
        ),
        ProjectItem(
            id = "proj_shipment_portal",
            title = "Shipment tracking customer portal",
            status = "Completed",
            category = "Website",
            summary = "A self-service portal where shippers follow their cargo in real time."
        )
    )

    val academyPrograms = listOf(
        AcademyProgram(
            id = "course_web_fundamentals",
            title = "Web development fundamentals",
            programType = "TRAINING",
            level = "Beginner",
            duration = "8 weeks",
            headline = "Learn to build and publish a responsive website.",
            description = "A practical introduction to HTML, CSS and JavaScript, ending with a small website you publish yourself."
        ),
        AcademyProgram(
            id = "course_mobile_dev",
            title = "Mobile app development",
            programType = "TRAINING",
            level = "Intermediate",
            duration = "10 weeks",
            headline = "Build your first mobile application.",
            description = "Design, build and test a simple mobile app from idea to working prototype."
        ),
        AcademyProgram(
            id = "course_digital_skills",
            title = "Digital skills certificate",
            programType = "CERTIFICATION",
            level = "Beginner",
            duration = "4 weeks",
            headline = "Earn a CODIVA certificate in everyday digital skills.",
            description = "Complete the programme and an assessment to receive a CODIVA certificate of completion."
        )
    )

    val newsArticles = listOf(
        NewsArticle(
            id = "news_monrovia_office",
            title = "CODIVA opens new Monrovia office",
            summary = "Our new office gives our team room to grow and welcomes clients in person.",
            category = "Announcement",
            publishedDate = "Oct 20, 2026",
            fullStory = "Our new Monrovia workspace brings engineering, product design, and client support under one roof. Organizations can book in-person discovery sessions, collaborate with our delivery leads, and attend community coding workshops."
        ),
        NewsArticle(
            id = "news_mobile_banking",
            title = "Lessons from building a mobile banking app in Liberia",
            summary = "What we learned about connectivity, trust and design when building for everyday users.",
            category = "Article",
            publishedDate = "Oct 2, 2026",
            fullStory = "Designing financial software for low-bandwidth environments taught our team to prioritize offline resilience, clear transaction receipts, and plain-language confirmation screens that earn immediate user trust."
        ),
        NewsArticle(
            id = "news_ict_forum",
            title = "CODIVA joins ICT sector forum",
            summary = "CODIVA will sit on the steering group of a new forum for software companies.",
            category = "Press",
            publishedDate = "Sep 15, 2026",
            fullStory = "Alongside regional technology founders and educators, CODIVA Technologies will contribute to open engineering standards, youth internship pathways, and digital public infrastructure initiatives."
        ),
        NewsArticle(
            id = "news_web_accessibility",
            title = "Designing for web accessibility",
            summary = "Accessible design is good design, and it is cheaper to build in from the start.",
            category = "Article",
            publishedDate = "Aug 28, 2026",
            fullStory = "High-contrast typography, generous tap targets, and screen-reader semantics are built into every CODIVA web and Android release from day one—improving usability for every customer."
        )
    )

    val careerRoles = listOf(
        CareerRole(
            id = "role_senior_swe",
            title = "Senior Software Engineer",
            department = "Engineering",
            location = "Monrovia, Liberia",
            employmentType = "Full-time",
            closingDate = "Dec 15, 2026",
            summary = "Lead architecture and delivery of scalable web and cloud software solutions for enterprise and community clients."
        ),
        CareerRole(
            id = "role_uiux_designer",
            title = "UI/UX Designer",
            department = "Design",
            location = "Monrovia, Liberia (hybrid)",
            employmentType = "Full-time",
            closingDate = "Dec 1, 2026",
            summary = "Craft intuitive mobile and web interfaces tested directly with everyday users across Liberia and global markets."
        ),
        CareerRole(
            id = "role_ops_coordinator",
            title = "Operations Coordinator",
            department = "Operations",
            location = "Monrovia, Liberia",
            employmentType = "Part-time",
            closingDate = "Nov 30, 2026",
            summary = "Coordinate client onboarding, Monrovia office logistics, training cohorts, and project milestone tracking."
        )
    )

    val teamMembers = listOf(
        TeamMember(
            name = "Ebenezer Yeapeh Jr",
            role = "Founder and Chief Executive Officer",
            group = "Founder",
            department = "Executive",
            bio = "Ebenezer founded CODIVA Technologies to build software that serves Liberian businesses and communities with a global vision.",
            initials = "EY"
        ),
        TeamMember(
            name = "Grace Johnson",
            role = "Operations Director",
            group = "Management",
            department = "Operations",
            bio = "Grace leads client delivery and keeps every project on schedule and on budget.",
            initials = "GJ"
        ),
        TeamMember(
            name = "Joseph Tubman",
            role = "Finance and Administration Manager",
            group = "Management",
            department = "Finance & Admin",
            bio = "Joseph oversees budgets, contracts and the company's day-to-day administration.",
            initials = "JT"
        ),
        TeamMember(
            name = "Mariam Kamara",
            role = "Lead Developer",
            group = "Developers",
            department = "Engineering",
            bio = "Mariam leads the development team on web and mobile products, with a focus on clean, tested code.",
            initials = "MK"
        ),
        TeamMember(
            name = "Abel Sirleaf",
            role = "Mobile Developer",
            group = "Developers",
            department = "Engineering",
            bio = "Abel builds Android and iOS apps and works closely with designers to make them feel effortless.",
            initials = "AS"
        ),
        TeamMember(
            name = "Samuel Kollie",
            role = "Client Support Specialist",
            group = "Staff",
            department = "Support",
            bio = "Samuel is the first point of contact for clients after launch, handling support requests and updates.",
            initials = "SK"
        )
    )

    val departments = listOf(
        DepartmentSummary("Software Engineering", "Mariam Kamara", 12, 6, "Web, Mobile & Enterprise Platforms"),
        DepartmentSummary("CODIVA Academy", "Abel Sirleaf", 6, 3, "Training, Cohorts & Certification"),
        DepartmentSummary("Operations & Delivery", "Grace Johnson", 5, 4, "Client Onboarding & Project Timelines"),
        DepartmentSummary("Finance & Admin", "Joseph Tubman", 3, 2, "Billing, Invoices & Payroll")
    )

    val initialWorkspaceRecords = listOf(
        SeedWorkspaceItem("seed_usr_1", WorkspaceModuleType.USER, "Ebenezer Yeapeh Jr.", "ebenezeryeapeh99@gmail.com", "Founder & CEO · Super Administrator", "SUPER_ADMIN", "ACTIVE"),
        SeedWorkspaceItem("seed_usr_2", WorkspaceModuleType.USER, "Grace Johnson", "grace.johnson@codiva.com", "Operations Director · Monrovia", "HOD", "ACTIVE"),
        SeedWorkspaceItem("seed_usr_3", WorkspaceModuleType.USER, "Mariam Kamara", "mariam.kamara@codiva.com", "Lead Developer & Web Cohort Lecturer", "LECTURER", "ACTIVE"),
        SeedWorkspaceItem("seed_usr_4", WorkspaceModuleType.USER, "Samuel Kollie", "samuel.kollie@codiva.com", "Client Support Specialist", "STAFF", "ACTIVE"),
        SeedWorkspaceItem("seed_usr_5", WorkspaceModuleType.USER, "Martha Dennis", "martha.dennis@example.com", "Academy Parent · Cohort 4", "PARENT", "PENDING"),

        SeedWorkspaceItem("seed_emp_1", WorkspaceModuleType.EMPLOYEE, "Mariam Kamara", "Software Engineering", "Lead Developer · Monrovia HQ", "Engineering", "ACTIVE"),
        SeedWorkspaceItem("seed_emp_2", WorkspaceModuleType.EMPLOYEE, "Abel Sirleaf", "Software Engineering", "Mobile Developer · Android & iOS", "Engineering", "ACTIVE"),
        SeedWorkspaceItem("seed_emp_3", WorkspaceModuleType.EMPLOYEE, "Grace Johnson", "Operations & Delivery", "Operations Director", "Operations", "ACTIVE"),
        SeedWorkspaceItem("seed_emp_4", WorkspaceModuleType.EMPLOYEE, "Joseph Tubman", "Finance & Admin", "Finance & Administration Manager", "Finance", "ACTIVE"),

        SeedWorkspaceItem("seed_pay_1", WorkspaceModuleType.PAYMENT, "Clinic Booking App Milestone", "Monrovia Health Network", "Phase 2 Mobile API & UI Delivery", "Project", "PAID", 1250000L),
        SeedWorkspaceItem("seed_pay_2", WorkspaceModuleType.PAYMENT, "Mobile App Dev Cohort Fee", "CODIVA Academy Tuition", "10-week intermediate mobile cohort", "Tuition", "PAID", 15000L),
        SeedWorkspaceItem("seed_pay_3", WorkspaceModuleType.PAYMENT, "Co-operative Farm Portal", "Lofa Growers Co-op", "Deposit invoice #INV-2026-041", "Project", "PENDING", 450000L),

        SeedWorkspaceItem("seed_ann_1", WorkspaceModuleType.ANNOUNCEMENT, "CODIVA Academy Now Open", "Join our free and paid courses today.", "Enrollment open for Web, Mobile, and Digital Skills cohorts.", "IMPORTANT", "PUBLISHED"),
        SeedWorkspaceItem("seed_ann_2", WorkspaceModuleType.ANNOUNCEMENT, "Holiday Office Hours", "Monrovia office closes Dec 18 at 2 p.m.", "Support tickets remain monitored through Jan 5.", "IMPORTANT", "PUBLISHED"),

        SeedWorkspaceItem("seed_sup_1", WorkspaceModuleType.SUPPORT, "Portal Account Role Verification", "From: Martha Dennis (Parent)", "Requesting parent dashboard link to student cohort progress.", "Account", "OPEN"),
        SeedWorkspaceItem("seed_sup_2", WorkspaceModuleType.SUPPORT, "Shipment Portal SSL Renewal", "From: Atlantic Cargo Ltd", "Confirming automated certificate renewal status.", "Hosting", "RESOLVED"),

        SeedWorkspaceItem("seed_tsk_1", WorkspaceModuleType.TASK, "Review Cohort 4 HTML/CSS Submissions", "Assigned to: Engineering & Academy", "18 student projects awaiting rubric score.", "Academy", "IN_PROGRESS"),
        SeedWorkspaceItem("seed_tsk_2", WorkspaceModuleType.TASK, "Deploy Clinic Booking Staging Build", "Assigned to: Abel Sirleaf", "Prepare Android APK testing link for client review.", "Delivery", "COMPLETED")
    )

    val helpFaqs = listOf(
        HelpFaqItem(
            id = "faq_accounts",
            category = "Account & Sign-In",
            question = "How do new users create an account on CODIVA Technologies?",
            answer = "Open Sign In, select Create Account, pick your role (Member, User, Parent, Staff, Lecturer, or Partner), enter your display name and invite code if applicable, and authenticate with Google. Staff, Lecturer, and Partner accounts remain Pending until approved by the Super Administrator."
        ),
        HelpFaqItem(
            id = "faq_academy",
            category = "CODIVA Academy",
            question = "How do I enroll in courses and track progress?",
            answer = "Go to the Academy tab, choose Web Development Fundamentals, Mobile App Development, or Digital Skills Certificate, and tap Enroll in Program. Use Advance Progress as you complete modules to earn completion status."
        ),
        HelpFaqItem(
            id = "faq_docs",
            category = "Documents & Bulletins",
            question = "Where do I find official documents posted by Administration?",
            answer = "Official documents, schedules, and notices published by the Super Administrator appear automatically on your Home feed, your Role Dashboard, and inside Portal -> Official Documents & Bulletins."
        ),
        HelpFaqItem(
            id = "faq_downloads",
            category = "Downloads & Stores",
            question = "How can users download CODIVA on Android, iPhone, Windows, or Web?",
            answer = "Visit Portal -> App Downloads & Releases to access the live links for Android (Google Play / APK), iPhone & iPad (Apple App Store), Windows Desktop/PWA, and Web (Codiva.com). Administrators can update store URLs and release versions anytime in Founder Dashboard."
        )
    )

    val initialPublications = listOf(
        SeedPublicationItem(
            docId = "pub_welcome_2026",
            title = "Welcome to CODIVA Technologies (${OFFICIAL_DOMAIN})",
            summary = "Official onboarding guide for Liberia and global members, students, staff, and partners.",
            content = "CODIVA Technologies connects engineering, Academy training, client projects, and community collaboration on one platform. Check your role dashboard daily for official documents, cohort schedules, and release updates.",
            category = PublicationCategory.GUIDE,
            targetRole = "ALL",
            documentUrl = PUBLIC_PORTAL_URL,
            documentLabel = "Open ${OFFICIAL_DOMAIN} Portal",
            expiresAtLabel = "Dec 31, 2026",
            status = PublicationStatus.PUBLISHED
        ),
        SeedPublicationItem(
            docId = "pub_academy_handbook",
            title = "CODIVA Academy 2026 Student Handbook & Calendar",
            summary = "Cohort schedule, grading policy, and certificate requirements for Web & Mobile tracks.",
            content = "Review cohort attendance expectations, weekly lab milestones, and project defense guidelines for all active CODIVA Academy students and lecturers.",
            category = PublicationCategory.DOCUMENT,
            targetRole = "ALL",
            documentUrl = PUBLIC_PORTAL_URL,
            documentLabel = "View Student Handbook",
            expiresAtLabel = "Nov 30, 2026",
            status = PublicationStatus.PUBLISHED
        ),
        SeedPublicationItem(
            docId = "pub_partner_brief",
            title = "2026 Liberia Digital Innovation Partnership Brief",
            summary = "Information for corporate sponsors, schools, and technology partners across Africa.",
            content = "Partner organizations can co-sponsor Academy scholarships, commission custom software delivery in Liberia, and review joint program milestones directly from the Partner Dashboard.",
            category = PublicationCategory.ANNOUNCEMENT,
            targetRole = "PARTNER",
            documentUrl = "",
            documentLabel = "",
            expiresAtLabel = "Dec 20, 2026",
            status = PublicationStatus.PUBLISHED
        )
    )

    val initialAppReleases = listOf(
        SeedReleaseItem(
            docId = "rel_android_play",
            platform = ReleasePlatform.ANDROID,
            versionName = "1.0.0",
            buildNumber = 1L,
            releaseNotes = "Official Android app for CODIVA Technologies with Google Sign-In, role dashboards, Academy tracking, and live Admin sync.",
            downloadUrl = "https://play.google.com/store/apps",
            status = ReleaseStatus.LIVE
        ),
        SeedReleaseItem(
            docId = "rel_ios_store",
            platform = ReleasePlatform.IOS,
            versionName = "1.0.0",
            buildNumber = 1L,
            releaseNotes = "iPhone & iPad release listing for Apple App Store distribution. Update URL once your App Store Connect listing is live.",
            downloadUrl = "https://apps.apple.com",
            status = ReleaseStatus.LIVE
        ),
        SeedReleaseItem(
            docId = "rel_windows_pwa",
            platform = ReleasePlatform.WINDOWS,
            versionName = "1.0.0",
            buildNumber = 1L,
            releaseNotes = "Installable Windows desktop & PWA package linked to ${OFFICIAL_DOMAIN}.",
            downloadUrl = PUBLIC_PORTAL_URL,
            status = ReleaseStatus.LIVE
        ),
        SeedReleaseItem(
            docId = "rel_web_codiva",
            platform = ReleasePlatform.WEB,
            versionName = "1.0.0",
            buildNumber = 1L,
            releaseNotes = "Responsive web portal for ${OFFICIAL_DOMAIN} desktop and mobile browsers.",
            downloadUrl = PUBLIC_PORTAL_URL,
            status = ReleaseStatus.LIVE
        )
    )
}
