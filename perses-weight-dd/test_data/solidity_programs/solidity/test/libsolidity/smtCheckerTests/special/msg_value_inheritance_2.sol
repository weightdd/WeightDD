pragma experimental SMTChecker;

contract A {
	uint public x = msg.value;
	constructor() {
		assert(x == 0); // should fail, if A is constructed as part of C, it can have any msg.value
	}
}

contract C is A {
	uint public v = msg.value; // 1
	constructor() A() payable {
		assert(v == 0); // should fail, C can be constructed with any msg.value
	}
}
// ----
// Warning 6328: (273-287): CHC: Assertion violation happens here.\nCounterexample:\nv = 1, x = 1\n\nTransaction trace:\nC.constructor(){ value: 1 }
// Warning 6328: (93-107): CHC: Assertion violation happens here.\nCounterexample:\nv = 0, x = 1\n\nTransaction trace:\nC.constructor(){ value: 1 }
