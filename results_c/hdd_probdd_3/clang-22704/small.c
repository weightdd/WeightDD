typedef
char
int8_t
;
typedef
int
int16_t
;
typedef
int
int32_t
;
typedef
char
uint8_t
;
typedef
unsigned
uint32_t
;
typedef
unsigned
uint64_t
;
struct
S0
{
int16_t
f0
;
int32_t
f4
;
int32_t
f6
;
uint8_t
f7
;
}
;
int32_t
g_2
;
struct
S0
g_20
;
int32_t
g_30
[
]
;
int8_t
func_76
(
uint8_t
,
uint32_t
,
uint32_t
,
uint64_t
,
uint8_t
)
;
uint8_t
func_65
(
)
{
int32_t
l_71
;
if
(
g_20
.
f6
)
goto
lbl_93
;
(
(
g_20
,
(
(
0xC4L
,
(
(
func_76
(
g_20
.
f0
,
l_71
,
l_71
,
g_2
,
g_30
[
0
]
)
)
)
)
)
)
)
;
{
uint8_t
l_1454
;
l_1454
=
0xEEACFBBFL
;
return
l_1454
;
}
lbl_93
:
g_20
.
f4
=
(
(
l_71
,
(
(
(
l_71
|
1L
)
)
)
)
)
;
func_65
(
)
;
{
uint8_t
l_1454
;
return
l_1454
;
}
}
int8_t
func_76
(
uint8_t
p_77
,
uint32_t
p_78
,
uint32_t
p_79
,
uint64_t
p_80
,
uint8_t
p_81
)
{
(
func_65
(
)
)
;
return
g_20
.
f7
;
}
int
main
(
)
{
}
