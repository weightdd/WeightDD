typedef
char
int8_t
;
typedef
int
int32_t
;
typedef
long
int64_t
;
typedef
char
uint8_t
;
typedef
int
uint16_t
;
typedef
unsigned
uint32_t
;
uint8_t
(
safe_rshift_func_uint8_t_u_s
)
(
uint8_t
left
,
int
right
)
{
return
(
(
0
)
)
?
(
(
left
)
)
:
(
left
>>
(
(
int
)
right
)
)
;
}
struct
S0
{
uint16_t
f0
;
}
;
struct
S1
{
signed
f2
:
18
;
signed
f5
:
1
;
}
;
int8_t
g_12
;
struct
S0
g_21
=
{
0xC0B8L
}
;
int32_t
g_37
;
static
int8_t
g_66
=
(
-
5L
)
;
uint32_t
g_528
;
int32_t
g_571
;
uint32_t
g_968
=
0xFF7B2476L
;
int8_t
func_14
(
int8_t
,
struct
S1
,
struct
S0
,
struct
S0
,
int32_t
)
;
int64_t
func_1
(
)
{
struct
S1
l_2
;
struct
S1
l_20
;
(
(
l_2
,
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
(
(
(
(
func_14
(
g_12
,
l_20
,
g_21
,
g_21
,
g_21
.
f0
)
)
)
)
)
,
g_571
)
)
)
)
)
)
)
)
)
&&
g_528
)
)
)
;
}
int8_t
func_14
(
int8_t
p_15
,
struct
S1
p_16
,
struct
S0
p_17
,
struct
S0
p_18
,
int32_t
p_19
)
{
struct
S1
l_59
=
{
1957
}
;
l_59
.
f5
=
(
safe_rshift_func_uint8_t_u_s
(
g_968
,
5
)
)
;
if
(
(
g_21
.
f0
)
)
{
int8_t
backup_0_8502_91289_l_59_f5
;
{
backup_0_8502_91289_l_59_f5
=
l_59
.
f5
;
l_59
.
f5
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
(
l_59
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
)
)
;
g_37
=
(
(
(
(
(
(
(
(
l_59
.
f2
)
)
%
(
l_59
.
f5
&
(
(
~
(
g_66
)
)
/
backup_0_8502_91289_l_59_f5
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
}
}
int
main
(
)
{
func_1
(
)
;
}
