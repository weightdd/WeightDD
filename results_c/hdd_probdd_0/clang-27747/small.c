typedef
char
int8_t
;
typedef
int
int32_t
;
typedef
unsigned
uint32_t
;
struct
S0
{
unsigned
f0
;
unsigned
f1
;
signed
f2
:
18
;
unsigned
f3
;
unsigned
f4
;
signed
f5
;
signed
f6
;
unsigned
f7
;
}
;
struct
S0
g_27
;
struct
S0
g_210
;
int32_t
func_2
(
)
{
for
(
;
;
)
{
struct
S0
l_2818
=
{
24
,
6738
,
426
,
165
,
390
}
;
uint32_t
BHbHbbl_2818_f0
;
int32_t
BHbHbdl_2818_f2
=
(
(
(
(
~
(
(
BHbHbbl_2818_f0
/
l_2818
.
f1
)
)
)
)
)
*
(
(
(
(
(
(
(
g_27
.
f7
)
)
)
)
)
)
)
)
;
if
(
(
g_210
.
f6
)
)
{
BHbHbdl_2818_f2
=
l_2818
.
f2
;
l_2818
.
f6
=
(
(
(
(
(
(
(
~
(
g_210
.
f3
)
)
)
)
)
)
)
)
;
}
l_2818
.
f2
=
BHbHbdl_2818_f2
;
int32_t
Brcrcl_2818_f2
=
(
(
(
(
(
(
(
(
l_2818
.
f6
)
)
^
(
(
(
(
(
g_27
.
f5
)
)
&&
l_2818
.
f2
)
)
)
)
)
)
)
)
)
;
int32_t
Brcrcbl_2818_f6
=
(
(
(
(
(
(
(
(
(
l_2818
.
f6
)
)
)
)
)
)
|
(
(
(
(
l_2818
.
f4
)
|
Brcrcl_2818_f2
)
)
)
)
*
(
g_210
.
f3
*
(
(
l_2818
.
f2
)
)
)
)
)
;
int8_t
Brcrccg_27_f5
=
(
(
(
(
(
(
(
(
(
(
l_2818
.
f6
)
)
)
)
)
)
&
Brcrcbl_2818_f6
)
|
(
(
-
(
(
l_2818
.
f4
)
&&
l_2818
.
f2
)
)
)
)
)
)
;
uint32_t
Brcrcdg_210_f3
=
(
(
(
(
(
(
l_2818
.
f4
)
)
)
)
)
&
(
(
(
Brcrccg_27_f5
)
)
)
)
;
g_210
.
f3
=
Brcrcdg_210_f3
;
}
}
int
main
(
)
{
}
