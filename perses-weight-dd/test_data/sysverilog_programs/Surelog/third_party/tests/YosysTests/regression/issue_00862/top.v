module test(input wire A);
  localparam TEST = 1;
  always_comb begin
    case (A)
      TEST: assert(1);
    endcase
  end
endmodule
