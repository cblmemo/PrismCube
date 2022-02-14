package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class IRPhiInstruction extends IRInstruction {
    private final IRRegister resultRegister;
    private final ArrayList<IROperand> values = new ArrayList<>();
    private final ArrayList<IRBasicBlock> blocks = new ArrayList<>();
    private final IRTypeSystem resultType;
    private final IRAllocaInstruction belongsTo;

    public IRPhiInstruction(IRBasicBlock parentBlock, IRRegister resultRegister, IRTypeSystem resultType, IRAllocaInstruction belongsTo) {
        super(parentBlock);
        this.resultRegister = resultRegister;
        this.resultType = resultType;
        this.belongsTo = belongsTo;
        resultRegister.setDef(this);
    }

    public void addCandidate(IROperand value, IRBasicBlock block) {
        values.add(value);
        blocks.add(block);
        value.addUser(this);
        block.getLabel().addUser(this);
    }

    public void replaceSourceBlock(IRBasicBlock oldBlock, IRBasicBlock newBlock) {
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i) == oldBlock) {
                blocks.set(i, newBlock);
                break;
            }
        }
    }

    public void forEachCandidate(BiConsumer<IRBasicBlock, IROperand> consumer) {
        for (int i = 0; i < blocks.size(); i++) consumer.accept(blocks.get(i), values.get(i));
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    @Override
    public boolean noUsersAndSafeToRemove() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(resultRegister).append(" = phi ").append(resultType).append(" ");
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) builder.append(", ");
            builder.append("[ ").append(values.get(i)).append(", ").append(blocks.get(i)).append(" ]");
        }
        return builder.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
