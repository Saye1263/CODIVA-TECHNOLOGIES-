const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const FOUNDER_UID = "founder_codiva_1";
const BOB_UID = "bob_codiva_2";
const LECTURER_UID = "lecturer_codiva_3";
const STAFF_UID = "staff_codiva_4";
const HOD_UID = "hod_codiva_5";
const FINANCE_UID = "finance_codiva_6";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users are denied across users, academy, projects, public_portal, and subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(FOUNDER_UID).get());
  await assertFails(unauthDb.collection("academy").doc("course_1").get());
  await assertFails(unauthDb.collection("projects").doc("proj_1").get());
  await assertFails(unauthDb.collection("public_portal").doc("config").get());
  await assertFails(unauthDb.collection("users").doc(FOUNDER_UID).collection("workspace_records").get());
  await assertFails(unauthDb.collection("users").doc(FOUNDER_UID).collection("audit_logs").get());
  await assertFails(unauthDb.collection("users").doc(FOUNDER_UID).collection("system_config").doc("settings").get());
});

test("Users RBAC: self-signup constraints, email verification guard, admin approval, and anti-escalation", async () => {
  const founderDb = testEnv.authenticatedContext(FOUNDER_UID, {
    email: "ebenezeryeapeh99@gmail.com",
    email_verified: true,
  }).firestore();
  const spoofedFounderDb = testEnv.authenticatedContext("spoof_uid", {
    email: "ebenezeryeapeh99@gmail.com",
    email_verified: false,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@example.com",
    email_verified: true,
  }).firestore();
  const lecturerDb = testEnv.authenticatedContext(LECTURER_UID, {
    email: "lecturer@example.com",
    email_verified: true,
  }).firestore();
  const past = new Date(Date.now() - 5000);

  // Unverified founder email spoof must be denied SUPER_ADMIN creation & user listing
  await assertFails(
    spoofedFounderDb.collection("users").doc("spoof_uid").set({
      userId: "spoof_uid",
      displayName: "Spoof",
      email: "ebenezeryeapeh99@gmail.com",
      accountRole: "SUPER_ADMIN",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertFails(spoofedFounderDb.collection("users").get());

  // Verified founder creates SUPER_ADMIN profile and publishes public_portal/config
  await assertSucceeds(
    founderDb.collection("users").doc(FOUNDER_UID).set({
      userId: FOUNDER_UID,
      displayName: "Ebenezer Yeapeh Jr",
      email: "ebenezeryeapeh99@gmail.com",
      accountRole: "SUPER_ADMIN",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    founderDb.collection("public_portal").doc("config").set({
      portalTitle: "CODIVA Technologies",
      publicUrl: "https://ais-pre-ssjxhvm5g2ud75qmux6y7y-15631185691.europe-west2.run.app",
      customDomain: "Codiva.com",
      welcomeMessage: "Welcome to CODIVA Technologies. Create an account to join.",
      inviteCode: "CODIVA-2026",
      portalPublished: true,
      allowPublicSignUp: true,
      requireAdminApproval: false,
      updatedBy: "ebenezeryeapeh99@gmail.com",
      updatedAt: past,
    })
  );

  // Non-admin cannot self-assign SUPER_ADMIN, HOD, or active LECTURER
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      email: "bob@example.com",
      accountRole: "SUPER_ADMIN",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertFails(
    lecturerDb.collection("users").doc(LECTURER_UID).set({
      userId: LECTURER_UID,
      displayName: "Dr. Ada",
      email: "lecturer@example.com",
      accountRole: "LECTURER",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Self-signup LECTURER starts PENDING; MEMBER can be ACTIVE or PENDING
  await assertSucceeds(
    lecturerDb.collection("users").doc(LECTURER_UID).set({
      userId: LECTURER_UID,
      displayName: "Dr. Ada",
      email: "lecturer@example.com",
      accountRole: "LECTURER",
      membershipStatus: "PENDING",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      email: "bob@example.com",
      accountRole: "MEMBER",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Bob cannot promote himself to LECTURER/STAFF/HOD or read Founder PII
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).update({
      accountRole: "LECTURER",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobDb.collection("users").get());
  await assertFails(bobDb.collection("users").doc(FOUNDER_UID).get());

  // PENDING lecturer cannot approve herself or write to academy yet
  await assertFails(
    lecturerDb.collection("users").doc(LECTURER_UID).update({
      membershipStatus: "ACTIVE",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    lecturerDb.collection("academy").doc("course_pending").set({
      title: "Unapproved Course",
      programType: "TRAINING",
      level: "Beginner",
      duration: "6 weeks",
      description: "Attempt by pending lecturer to create a course.",
      instructorId: LECTURER_UID,
      instructorName: "Dr. Ada",
      status: "DRAFT",
      createdBy: LECTURER_UID,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Founder lists registered users and approves lecturer
  await assertSucceeds(founderDb.collection("users").get());
  await assertSucceeds(
    founderDb.collection("users").doc(LECTURER_UID).update({
      membershipStatus: "ACTIVE",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Academy RBAC: Super Admin, HOD, Lecturer, and Member permissions with schema and terminal state guards", async () => {
  const founderDb = testEnv.authenticatedContext(FOUNDER_UID, {
    email: "ebenezeryeapeh99@gmail.com",
    email_verified: true,
  }).firestore();
  const lecturerDb = testEnv.authenticatedContext(LECTURER_UID, {
    email: "lecturer@example.com",
    email_verified: true,
  }).firestore();
  const hodDb = testEnv.authenticatedContext(HOD_UID, {
    email: "hod@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@example.com",
    email_verified: true,
  }).firestore();
  const past = new Date(Date.now() - 5000);

  // Seed active profiles via founder/admin
  await assertSucceeds(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob Learner",
      email: "bob@example.com",
      accountRole: "MEMBER",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    lecturerDb.collection("users").doc(LECTURER_UID).set({
      userId: LECTURER_UID,
      displayName: "Dr. Ada",
      email: "lecturer@example.com",
      accountRole: "LECTURER",
      membershipStatus: "PENDING",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    founderDb.collection("users").doc(LECTURER_UID).update({
      membershipStatus: "ACTIVE",
      updatedAt: past,
    })
  );
  await assertSucceeds(
    hodDb.collection("users").doc(HOD_UID).set({
      userId: HOD_UID,
      displayName: "HOD Samuel",
      email: "hod@example.com",
      accountRole: "MEMBER",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    founderDb.collection("users").doc(HOD_UID).update({
      accountRole: "HOD",
      updatedAt: past,
    })
  );

  // Active lecturer creates a DRAFT course
  await assertSucceeds(
    lecturerDb.collection("academy").doc("course_mobile_101").set({
      title: "Mobile App Development",
      programType: "TRAINING",
      level: "Intermediate",
      duration: "8 weeks",
      description: "Build production Android Jetpack Compose apps with Firebase.",
      instructorId: LECTURER_UID,
      instructorName: "Dr. Ada",
      status: "DRAFT",
      createdBy: LECTURER_UID,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Member cannot read DRAFT course or create/update courses
  await assertFails(bobDb.collection("academy").doc("course_mobile_101").get());
  await assertFails(
    bobDb.collection("academy").doc("course_hack").set({
      title: "Unauthorized Course",
      programType: "TRAINING",
      level: "Beginner",
      duration: "4 weeks",
      description: "Members must never be able to create Academy courses.",
      instructorId: BOB_UID,
      instructorName: "Bob",
      status: "PUBLISHED",
      createdBy: BOB_UID,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Lecturer cannot inject ghost field or reassign instructorId
  await assertFails(
    lecturerDb.collection("academy").doc("course_mobile_101").update({
      instructorId: BOB_UID,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    lecturerDb.collection("academy").doc("course_mobile_101").update({
      status: "PUBLISHED",
      isVerified: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Lecturer publishes own course -> Active Member can read & query PUBLISHED courses
  await assertSucceeds(
    lecturerDb.collection("academy").doc("course_mobile_101").update({
      status: "PUBLISHED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(bobDb.collection("academy").doc("course_mobile_101").get());
  await assertSucceeds(bobDb.collection("academy").where("status", "==", "PUBLISHED").get());

  // HOD archives course -> Lecturer cannot modify ARCHIVED course or delete it; Super Admin can
  await assertSucceeds(
    hodDb.collection("academy").doc("course_mobile_101").update({
      status: "ARCHIVED",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    lecturerDb.collection("academy").doc("course_mobile_101").update({
      status: "PUBLISHED",
      updatedAt: new Date(Date.now() - 200),
    })
  );
  await assertFails(hodDb.collection("academy").doc("course_mobile_101").delete());
  await assertSucceeds(founderDb.collection("academy").doc("course_mobile_101").delete());
});

test("Projects RBAC: Super Admin, HOD, Staff, Finance, and Client visibility and field-level isolation", async () => {
  const founderDb = testEnv.authenticatedContext(FOUNDER_UID, {
    email: "ebenezeryeapeh99@gmail.com",
    email_verified: true,
  }).firestore();
  const staffDb = testEnv.authenticatedContext(STAFF_UID, {
    email: "staff@example.com",
    email_verified: true,
  }).firestore();
  const financeDb = testEnv.authenticatedContext(FINANCE_UID, {
    email: "finance@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@example.com",
    email_verified: true,
  }).firestore();
  const past = new Date(Date.now() - 5000);

  // Setup roles
  await assertSucceeds(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob Client",
      email: "bob@example.com",
      accountRole: "MEMBER",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    staffDb.collection("users").doc(STAFF_UID).set({
      userId: STAFF_UID,
      displayName: "Staff Musa",
      email: "staff@example.com",
      accountRole: "STAFF",
      membershipStatus: "PENDING",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    founderDb.collection("users").doc(STAFF_UID).update({
      membershipStatus: "ACTIVE",
      updatedAt: past,
    })
  );
  await assertSucceeds(
    financeDb.collection("users").doc(FINANCE_UID).set({
      userId: FINANCE_UID,
      displayName: "Finance Grace",
      email: "finance@example.com",
      accountRole: "MEMBER",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    founderDb.collection("users").doc(FINANCE_UID).update({
      accountRole: "FINANCE",
      updatedAt: past,
    })
  );

  // Active Staff creates a CLIENT_ONLY project assigned to Bob
  await assertSucceeds(
    staffDb.collection("projects").doc("proj_clinic_1").set({
      title: "Monrovia Clinic Portal",
      category: "Web & Mobile",
      summary: "Patient appointment scheduling and billing system.",
      ownerId: STAFF_UID,
      leadUid: STAFF_UID,
      clientUid: BOB_UID,
      visibility: "CLIENT_ONLY",
      status: "PLANNING",
      budgetCents: 250000,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Client Bob can read and query his assigned project, but cannot edit or delete it
  await assertSucceeds(bobDb.collection("projects").doc("proj_clinic_1").get());
  await assertSucceeds(bobDb.collection("projects").where("clientUid", "==", BOB_UID).get());
  await assertFails(
    bobDb.collection("projects").doc("proj_clinic_1").update({
      budgetCents: 0,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Staff can update summary & status, but cannot modify budgetCents
  await assertFails(
    staffDb.collection("projects").doc("proj_clinic_1").update({
      budgetCents: 999999,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    staffDb.collection("projects").doc("proj_clinic_1").update({
      status: "IN_PROGRESS",
      summary: "Patient scheduling API and Android client in active sprint.",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Finance can update budgetCents only, cannot alter status
  await assertFails(
    financeDb.collection("projects").doc("proj_clinic_1").update({
      status: "COMPLETED",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    financeDb.collection("projects").doc("proj_clinic_1").update({
      budgetCents: 300000,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Once marked COMPLETED, non-SuperAdmin cannot modify it; only Super Admin can delete
  await assertSucceeds(
    staffDb.collection("projects").doc("proj_clinic_1").update({
      status: "COMPLETED",
      updatedAt: new Date(Date.now() - 300),
    })
  );
  await assertFails(
    staffDb.collection("projects").doc("proj_clinic_1").update({
      status: "IN_PROGRESS",
      updatedAt: new Date(Date.now() - 100),
    })
  );
  await assertFails(staffDb.collection("projects").doc("proj_clinic_1").delete());
  await assertSucceeds(founderDb.collection("projects").doc("proj_clinic_1").delete());
});

test("Workspace records, append-only audit logs, and system settings enforce strict schema and ownership", async () => {
  const founderDb = testEnv.authenticatedContext(FOUNDER_UID, {
    email: "ebenezeryeapeh99@gmail.com",
    email_verified: true,
  }).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    founderDb.collection("users").doc(FOUNDER_UID).collection("workspace_records").doc("rec_pay_1").set({
      userId: FOUNDER_UID,
      moduleType: "PAYMENT",
      title: "Academy Tuition - Cohort 4",
      subtitle: "Mobile App Development",
      detail: "Verified invoice payment via Monrovia portal",
      category: "Tuition",
      status: "PAID",
      amountCents: 15000,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    founderDb.collection("users").doc(FOUNDER_UID).collection("audit_logs").doc("log_1").set({
      userId: FOUNDER_UID,
      actorEmail: "ebenezeryeapeh99@gmail.com",
      action: "PAYMENT_RECORDED",
      target: "Academy Tuition - Cohort 4",
      severity: "INFO",
      createdAt: past,
    })
  );

  await assertFails(
    founderDb.collection("users").doc(FOUNDER_UID).collection("audit_logs").doc("log_1").update({
      severity: "CRITICAL",
    })
  );

  await assertSucceeds(
    founderDb.collection("users").doc(FOUNDER_UID).collection("system_config").doc("settings").set({
      userId: FOUNDER_UID,
      autoApproveMembers: false,
      maintenanceMode: false,
      paymentsEnabled: true,
      auditLoggingEnabled: true,
      holidayNoticeEnabled: true,
      updatedAt: past,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(FOUNDER_UID).collection("workspace_records").get());
  await assertFails(bobDb.collection("users").doc(FOUNDER_UID).collection("audit_logs").get());
});

test("AdminPublications & AppReleases RBAC and PARTNER role self-signup", async () => {
  const founderDb = testEnv.authenticatedContext(FOUNDER_UID, {
    email: "ebenezeryeapeh99@gmail.com",
    email_verified: true,
  }).firestore();
  const partnerDb = testEnv.authenticatedContext("partner_codiva_7", {
    email: "partner@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@example.com",
    email_verified: true,
  }).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob Member",
      email: "bob@example.com",
      accountRole: "MEMBER",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );

  // PARTNER self-signup must start PENDING
  await assertFails(
    partnerDb.collection("users").doc("partner_codiva_7").set({
      userId: "partner_codiva_7",
      displayName: "Liberia Tech Partner",
      email: "partner@example.com",
      accountRole: "PARTNER",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    partnerDb.collection("users").doc("partner_codiva_7").set({
      userId: "partner_codiva_7",
      displayName: "Liberia Tech Partner",
      email: "partner@example.com",
      accountRole: "PARTNER",
      membershipStatus: "PENDING",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Only Founder/Admin can create, edit, expire, or delete /publications and /app_releases
  await assertFails(
    bobDb.collection("publications").doc("pub_hack").set({
      title: "Unauthorized Bulletin",
      summary: "Attempt by member",
      content: "Members cannot publish official CODIVA bulletins.",
      category: "ANNOUNCEMENT",
      targetRole: "ALL",
      documentUrl: "",
      documentLabel: "",
      expiresAtLabel: "",
      status: "PUBLISHED",
      publishedBy: "bob@example.com",
      createdBy: BOB_UID,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    founderDb.collection("publications").doc("pub_guide_1").set({
      title: "CODIVA 2026 Student Handbook",
      summary: "Official Academy rules, calendar, and certification guide.",
      content: "Download the handbook and review cohort schedules for Web and Mobile tracks.",
      category: "DOCUMENT",
      targetRole: "ALL",
      documentUrl: "https://ais-pre-ssjxhvm5g2ud75qmux6y7y-15631185691.europe-west2.run.app",
      documentLabel: "Open Handbook",
      expiresAtLabel: "Dec 31, 2026",
      status: "PUBLISHED",
      publishedBy: "ebenezeryeapeh99@gmail.com",
      createdBy: FOUNDER_UID,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(bobDb.collection("publications").where("status", "==", "PUBLISHED").get());
  await assertSucceeds(
    founderDb.collection("publications").doc("pub_guide_1").update({
      status: "EXPIRED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobDb.collection("publications").doc("pub_guide_1").get());
  await assertSucceeds(founderDb.collection("publications").doc("pub_guide_1").delete());

  await assertSucceeds(
    founderDb.collection("app_releases").doc("rel_ios_1").set({
      platform: "IOS",
      versionName: "1.0.0",
      buildNumber: 1,
      releaseNotes: "CODIVA Technologies iPhone & iPad release listing.",
      downloadUrl: "https://apps.apple.com",
      status: "LIVE",
      updatedBy: "ebenezeryeapeh99@gmail.com",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(bobDb.collection("app_releases").where("status", "==", "LIVE").get());
  await assertFails(bobDb.collection("app_releases").doc("rel_ios_1").delete());
  await assertSucceeds(founderDb.collection("app_releases").doc("rel_ios_1").delete());
});

test("Hardened RBAC boundaries: Parent, Suspended accounts, Lecturer draft isolation, and HOD/Staff separation of duties", async () => {
  const founderDb = testEnv.authenticatedContext(FOUNDER_UID, {
    email: "ebenezeryeapeh99@gmail.com",
    email_verified: true,
  }).firestore();
  const parentDb = testEnv.authenticatedContext("parent_codiva_8", {
    email: "parent@example.com",
    email_verified: true,
  }).firestore();
  const lecturerDb = testEnv.authenticatedContext(LECTURER_UID, {
    email: "lecturer@example.com",
    email_verified: true,
  }).firestore();
  const hodDb = testEnv.authenticatedContext(HOD_UID, {
    email: "hod@example.com",
    email_verified: true,
  }).firestore();
  const staffDb = testEnv.authenticatedContext(STAFF_UID, {
    email: "staff@example.com",
    email_verified: true,
  }).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    parentDb.collection("users").doc("parent_codiva_8").set({
      userId: "parent_codiva_8",
      displayName: "Parent Comfort",
      email: "parent@example.com",
      accountRole: "PARENT",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    lecturerDb.collection("users").doc(LECTURER_UID).set({
      userId: LECTURER_UID,
      displayName: "Dr. Ada",
      email: "lecturer@example.com",
      accountRole: "LECTURER",
      membershipStatus: "PENDING",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    founderDb.collection("users").doc(LECTURER_UID).update({
      membershipStatus: "ACTIVE",
      updatedAt: past,
    })
  );
  await assertSucceeds(
    hodDb.collection("users").doc(HOD_UID).set({
      userId: HOD_UID,
      displayName: "HOD Samuel",
      email: "hod@example.com",
      accountRole: "MEMBER",
      membershipStatus: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    founderDb.collection("users").doc(HOD_UID).update({
      accountRole: "HOD",
      updatedAt: past,
    })
  );
  await assertSucceeds(
    staffDb.collection("users").doc(STAFF_UID).set({
      userId: STAFF_UID,
      displayName: "Staff Musa",
      email: "staff@example.com",
      accountRole: "STAFF",
      membershipStatus: "PENDING",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    founderDb.collection("users").doc(STAFF_UID).update({
      membershipStatus: "ACTIVE",
      updatedAt: past,
    })
  );

  // Lecturer cannot spoof another instructorId when creating a course
  await assertFails(
    lecturerDb.collection("academy").doc("course_spoof").set({
      title: "Spoofed Instructor Course",
      programType: "TRAINING",
      level: "Beginner",
      duration: "4 weeks",
      description: "Attempt to assign another UID as instructorId.",
      instructorId: HOD_UID,
      instructorName: "HOD Samuel",
      status: "DRAFT",
      createdBy: LECTURER_UID,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    lecturerDb.collection("academy").doc("course_real").set({
      title: "Cloud Engineering",
      programType: "CERTIFICATION",
      level: "Advanced",
      duration: "10 weeks",
      description: "Production cloud architecture and Firestore security.",
      instructorId: LECTURER_UID,
      instructorName: "Dr. Ada",
      status: "PUBLISHED",
      createdBy: LECTURER_UID,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Parent can read PUBLISHED academy course, cannot write
  await assertSucceeds(parentDb.collection("academy").doc("course_real").get());
  await assertFails(
    parentDb.collection("academy").doc("course_real").update({
      title: "Parent Edit Attempt",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Staff creates project, cannot ARCHIVE it; HOD cannot alter budgetCents
  await assertSucceeds(
    staffDb.collection("projects").doc("proj_public_1").set({
      title: "Monrovia Digital Hub",
      category: "Infrastructure",
      summary: "Community innovation and digital training facility.",
      ownerId: STAFF_UID,
      leadUid: STAFF_UID,
      visibility: "PUBLIC",
      status: "IN_PROGRESS",
      budgetCents: 500000,
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(parentDb.collection("projects").doc("proj_public_1").get());
  await assertFails(
    staffDb.collection("projects").doc("proj_public_1").update({
      status: "ARCHIVED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    hodDb.collection("projects").doc("proj_public_1").update({
      budgetCents: 900000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Suspending parent revokes profile update and collection reads
  await assertSucceeds(
    founderDb.collection("users").doc("parent_codiva_8").update({
      membershipStatus: "SUSPENDED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    parentDb.collection("users").doc("parent_codiva_8").update({
      displayName: "Bypass Attempt",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(parentDb.collection("academy").doc("course_real").get());
  await assertFails(parentDb.collection("projects").doc("proj_public_1").get());
});

