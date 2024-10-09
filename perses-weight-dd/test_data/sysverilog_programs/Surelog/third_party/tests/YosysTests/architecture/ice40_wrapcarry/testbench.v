module testbench;
    reg [7:0] in;

	wire [3:0] outA,outB;
	wire [3:0] poutA,poutB;

    initial begin
       //  $dumpfile("testbench.vcd");
       //  $dumpvars(0, testbench);

        #5 in = 0;
        repeat (10000) begin
            #5 in = in + 1;
        end

        $display("OKAY");
    end

    top uut (
	.x(in[3:0]),
	.y(in[7:4]),
	.A(outA),
	.B(outB)
	);


	assign poutB =  in[3:0] / in[7:4];

	check_comb div_test(in[7:4], outB, poutB);

endmodule

module check_comb(input [3:0] div, input [3:0] test, input [3:0] pat);
    always @*
    begin
        #1;
        if (div != 4'b0000)
			if (test !== pat)
			begin
				$display("ERROR: ASSERTION FAILED in %m:",$time," ",test," ",pat);
				$stop;
			end
    end
endmodule

