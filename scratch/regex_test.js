const newZodCode = `import { z } from 'zod';
export const taskSchema = z.object({
  username: z.string().min(8).max(18),
})`;

const regex = /^\s*([a-zA-Z0-9_]+):\s*(z\.(?:string|number|any|boolean)\(\)|z\.array\(z\.string\(\)\))(.*?)(?:\/\/\s*\[([^\]]+)\])?\s*$/gm;
let match;
while ((match = regex.exec(newZodCode)) !== null) {
    console.log("Match found:");
    console.log("1:", match[1]);
    console.log("2:", match[2]);
    console.log("3:", match[3]);
    console.log("4:", match[4]);
}
