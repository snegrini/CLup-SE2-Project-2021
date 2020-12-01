--------------------------------
-- Signatures
---------------------------------
sig Date {}
sig Time {}
sig DateTime {
	date: Date,
	time: Time
}

sig StoreName {}
sig Address {}
sig PecEmail {}
sig Email {}
sig CategoryName {}
sig Password {}
sig PhoneNumber {}
sig UserCode {}
sig PassCode {}

sig Store {
	name: StoreName,
	address: Address,
	timetable: some OpeningHours,
	pecEmail: PecEmail,
	phone: PhoneNumber,
	storeCap: Int,
	itemCategories: some ItemCategory,
	manager: Manager,
	employee: Employee,
	queue: Queue,
	timeSlots: set TimeSlot,
	customersInside: Int
}
{
	storeCap > 0
	customersInside >= 0
}

sig OpeningHours {
	from: Time,
	to: Time
} {from != to}

sig ItemCategory {
	name: CategoryName
}

sig Queue {
	tickets: set Ticket
}

abstract sig StorePass {
	date: Date,
	arrivalTime: Time,
	issuedAt: DateTime,
	passCode: PassCode,
	passStatus: PassStatus
}

sig Booking extends StorePass {
	email: Email,
	departureTime: Time,
	bookingStatus: BookingStatus,
	itemCategories: some ItemCategory
}

sig Ticket extends StorePass {
	queueNumber: Int
} {
	queueNumber > 0
}

abstract sig PassStatus {}
one sig Valid extends PassStatus {}
one sig Active extends PassStatus {}
one sig Expired extends PassStatus {}

abstract sig BookingStatus {}
one sig Pending extends BookingStatus {}
one sig Confirmed extends BookingStatus {}

sig TimeSlot {
	date: Date,
	timeStart: Time,
	timeEnd: Time,
	bookings: set Booking
} {timeStart != timeEnd}

abstract sig User {
	userCode: UserCode,
	password: Password
}

sig Manager extends User {}
sig Employee extends User {}
sig Admin extends User {}

---------------------------------
-- Facts
---------------------------------
-- Store
fact storeNameAreUnique {
	no disj s1, s2: Store | s1.name = s2.name
}

fact noStoreNameWithoutStore {
	all sn: StoreName | one s: Store |  s.name = sn
}

fact storeEmailAreUnique {
	no disj s1, s2: Store | s1.pecEmail = s2.pecEmail
}

fact noStoreEmailWithoutStore {
	all pe: PecEmail | one s: Store |  s.pecEmail = pe
}

fact phoneNumberAreUnique {
	no disj s1, s2: Store | s1.phone = s2.phone
}

fact noPhoneWithoutStore {
	all p: PhoneNumber | one s: Store |  s.phone = p
}

fact addressAreUnique {
	no disj s1, s2: Store | s1.address = s2.address
}

fact noAddressWithoutStore {
	all a: Address | one s: Store |  s.address = a
}

fact maxCustomerInside {
	all s: Store | s.customersInside <= s.storeCap
}

-- Manager
fact oneManagerBelongToOneStore {
	no disj s1, s2: Store | s1.manager = s2.manager
}

fact  noManagerWithoutStore {
	all m: Manager | one s: Store |  s.manager = m
}

-- Employee
fact oneEmployeeBelongToOneStore {
	no disj s1, s2: Store | s1.employee = s2.employee
}

fact noEmployeeWithoutStore {
	all e: Employee | one s: Store |  s.employee = e
}

-- Queue
fact oneQueueBelongToOneStore {
	no disj s1, s2: Store | s1.queue = s2.queue
}

fact noQueueWithoutStore {
	all q: Queue | one s: Store |  q in s.queue
}

-- TimeSlot
fact oneTimeSlotsBelongToOneStore {
	all t: TimeSlot | no disj s1, s2: Store | t in s1.timeSlots and t in s2.timeSlots
}

fact noTimeSlotWithoutStore {
	all ts: TimeSlot | one s: Store |  ts in s.timeSlots
}

-- ItemCategory
fact oneItemCategoryBelongToOneStore {
	all i: ItemCategory | no disj s1, s2: Store | i in s1.itemCategories and i in s2.itemCategories
}

fact noItemCategoryWithoutStore {
	all i: ItemCategory | one s: Store |  i in s.itemCategories
}

-- Category Name
fact itemCatergoryNameAreUnique {
	no disj i1, i2: ItemCategory | i1.name = i2.name
}

fact noCategoryNameWithoutItemCategory {
	all cn: CategoryName | one ic: ItemCategory |  ic.name = cn
}

-- OpeningHours
fact oneOpeningHoursBelongToOneStore {
	all o: OpeningHours | no disj s1, s2: Store | o in s1.timetable and o in s2.timetable
}

fact noOpeningHoursWithoutStore {
	all o: OpeningHours | one s: Store |  o in s.timetable
}

-- UserCode
fact userCodeAreUnique {
	no disj u1, u2: User | u1.userCode = u2.userCode
}

fact noUserCodeWithoutUser {
	all uc: UserCode | one u: User |  u.userCode = uc
}

-- Password
fact noPasswordWithoutUser {
	all p: Password | one u: User |  u.password = p
}

-- PassCode
fact passCodeAreUnique {
	no disj sp1, sp2: StorePass | sp1.passCode = sp2.passCode
}

fact noPassCodeWithoutStorePass {
	all pc: PassCode | one sp: StorePass |  sp.passCode = pc
}

-- Ticket
fact oneTicketBelongToOneQueue {
	all t: Ticket | no disj q1, q2: Queue | t in q1.tickets and t in q2.tickets
}

fact onlyValidTicketInQueue {
	all t: Ticket | one q: Queue |  t in q.tickets iff t.passStatus = Valid
}

fact noSameTicketQueueNumberInSameQueue {
	all q: Queue | no disj t1, t2: Ticket | t1 in q.tickets and t2 in q.tickets and t1.queueNumber = t2.queueNumber
}

-- Booking
fact oneBookingBelongToSameStoreSlots {
	all b: Booking | no disj s1, s2: Store | b in s1.timeSlots.bookings and b in s2.timeSlots.bookings
}

fact noBookingWithoutTimeslot {
	all b: Booking | some ts: TimeSlot |  b in ts.bookings
}

fact noTwoBookingOfSamePersonOnSameDay {
	no disj b1, b2: Booking | b1.date = b2.date and b1.email = b2.email
}

-- Booking Mail
fact noBookingEmailWithoutStore {
	all e: Email | one b: Booking | b.email  = e
}

-- Date
fact noDateWithoutStorePassOrTimeSlot {
	(all d: Date | one sp: StorePass | sp.date = d) or
	(all d: Date | one ts: TimeSlot | ts.date = d) or 
	(all d: Date | one dt: DateTime | dt.date = d)
}

-- Time
fact noTimeWithoutStorePass {
	(all t: Time | one sp: StorePass | sp.arrivalTime = t) or
	(all t: Time | one b: Booking | b.departureTime = t) or
	(all t: Time | one o: OpeningHours | o.from = t or o.to = t) or
	(all t: Time | one ts: TimeSlot | ts.timeStart = t or ts.timeEnd = t) or
	(all t: Time | one dt: DateTime | dt.time = t)
}

-- Date Time
fact noDateTimeWithoutStorePass {
	all dt: DateTime | one sp: StorePass | sp.issuedAt = dt
}

---------------------------------
-- Predicates
---------------------------------
pred world1 {
	#Store = 1
	#ItemCategory = 1
	#OpeningHours = 1
	#Admin = 0
	#TimeSlot = 0
	#Ticket = 2

	one t: Ticket |  (t.passStatus = Active or t.passStatus = Expired)
}
run world1 for 2

pred world2 [q: Queue] {
	#Store = 1
	#q.tickets = 0
	#Booking = 2
	#TimeSlot = 2
	
	one t: TimeSlot | #t.bookings = 0
}
run world2 for 2

pred world3 {
	#Store = 2
	#Queue.tickets = 0
	#TimeSlot = 0
	#ItemCategory = 2
}
run world3 for 5
