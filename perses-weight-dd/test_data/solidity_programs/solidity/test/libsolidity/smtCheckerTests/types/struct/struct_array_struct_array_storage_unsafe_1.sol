pragma experimental SMTChecker;

contract C {
	struct T {
		uint y;
		uint[] a;
	}
	struct S {
		uint x;
		T t;
		uint[] a;
		T[] ts;
	}
	S s1;
	function f() public {
		s1.x = 2;
		assert(s1.x != 2);
		s1.t.y = 3;
		assert(s1.t.y != 3);
		s1.a.push();
		s1.a.push();
		s1.a.push();
		s1.a[2] = 4;
		assert(s1.a[2] != 4);
		s1.ts.push();
		s1.ts.push();
		s1.ts.push();
		s1.ts.push();
		s1.ts.push();
		s1.ts.push();
		s1.ts[3].y = 5;
		assert(s1.ts[3].y != 5);
		s1.ts[4].a.push();
		s1.ts[4].a.push();
		s1.ts[4].a.push();
		s1.ts[4].a.push();
		s1.ts[4].a.push();
		s1.ts[4].a.push();
		s1.ts[4].a[5] = 6;
		assert(s1.ts[4].a[5] != 6);
	}
}
// ----
// Warning 6328: (181-198): CHC: Assertion violation happens here.\nCounterexample:\ns1 = {x: 2, t: {y: 0, a: []}, a: [], ts: []}\n\nTransaction trace:\nC.constructor()\nState: s1 = {x: 0, t: {y: 0, a: []}, a: [], ts: []}\nC.f()
// Warning 6328: (216-235): CHC: Assertion violation happens here.
// Warning 6328: (299-319): CHC: Assertion violation happens here.
// Warning 6328: (437-460): CHC: Assertion violation happens here.
// Warning 6328: (611-637): CHC: Assertion violation happens here.
